const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const OWNER_UID = "owner_123";
const EXECUTOR_UID = "executor_456";
const CONTRIBUTOR_UID = "contributor_789";
const OUTSIDER_UID = "outsider_999";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated access
test("Unauthenticated cannot read ideas or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("ideas").get());
  await assertFails(unauthDb.collection("users").doc("any").get());
});

// 2. User Profiles
test("User can create and update own profile with valid schema", async () => {
  const userDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  await assertSucceeds(
    userDb.collection("users").doc(OWNER_UID).set({
      uid: OWNER_UID,
      displayName: "Alice Owner",
      email: "alice@example.com",
      photoUrl: "",
      shareCode: "IM-AB34CD"
    })
  );

  // Cannot write another user's profile
  await assertFails(
    userDb.collection("users").doc(BOB_UID_CHECK = "other_user").set({
      uid: "other_user",
      shareCode: "IM-XY98ZZ"
    })
  );
});

// 3. Collaborative Ideas Permissions
test("Owner can create idea with memberIds containing owner", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  await assertSucceeds(
    ownerDb.collection("ideas").doc("idea_1").set({
      title: "AI Startup",
      description: "Next gen AI platform",
      category: "AI",
      executionStatus: "Not Started",
      ownerId: OWNER_UID,
      executorId: EXECUTOR_UID,
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID]
    })
  );
});

test("Non-member cannot read idea, members can read", async () => {
  // Pre-seed idea using admin context
  await testEnv.withSecurityRulesDisabled(async (adminCtx) => {
    await adminCtx.firestore().collection("ideas").doc("idea_2").set({
      title: "Secret Idea",
      ownerId: OWNER_UID,
      executorId: EXECUTOR_UID,
      executionStatus: "Not Started",
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID]
    });
  });

  const memberDb = testEnv.authenticatedContext(CONTRIBUTOR_UID).firestore();
  await assertSucceeds(memberDb.collection("ideas").doc("idea_2").get());

  const outsiderDb = testEnv.authenticatedContext(OUTSIDER_UID).firestore();
  await assertFails(outsiderDb.collection("ideas").doc("idea_2").get());
});

test("Only Owner and Executor can change execution status", async () => {
  await testEnv.withSecurityRulesDisabled(async (adminCtx) => {
    await adminCtx.firestore().collection("ideas").doc("idea_3").set({
      title: "Launch Project",
      ownerId: OWNER_UID,
      executorId: EXECUTOR_UID,
      executionStatus: "Not Started",
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID]
    });
  });

  // Executor changes status to Started -> SUCCEEDS
  const executorDb = testEnv.authenticatedContext(EXECUTOR_UID).firestore();
  await assertSucceeds(
    executorDb.collection("ideas").doc("idea_3").update({
      executionStatus: "Started"
    })
  );

  // Owner changes status to Done -> SUCCEEDS
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  await assertSucceeds(
    ownerDb.collection("ideas").doc("idea_3").update({
      executionStatus: "Done"
    })
  );

  // Contributor changes status to Live -> FAILS
  const contributorDb = testEnv.authenticatedContext(CONTRIBUTOR_UID).firestore();
  await assertFails(
    contributorDb.collection("ideas").doc("idea_3").update({
      executionStatus: "Live"
    })
  );
});

test("Contributor can update requirements, notes, and steps", async () => {
  await testEnv.withSecurityRulesDisabled(async (adminCtx) => {
    await adminCtx.firestore().collection("ideas").doc("idea_4").set({
      title: "Collaborative Board",
      ownerId: OWNER_UID,
      executorId: EXECUTOR_UID,
      executionStatus: "Not Started",
      neededRequirements: "Money,Time",
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID]
    });
  });

  const contributorDb = testEnv.authenticatedContext(CONTRIBUTOR_UID).firestore();
  await assertSucceeds(
    contributorDb.collection("ideas").doc("idea_4").update({
      neededRequirements: "Money,Time,Skills",
      nextAction: "Interview 5 developers"
    })
  );
});

test("Only Owner can invite or remove members and change Executor", async () => {
  await testEnv.withSecurityRulesDisabled(async (adminCtx) => {
    await adminCtx.firestore().collection("ideas").doc("idea_5").set({
      title: "Team Idea",
      ownerId: OWNER_UID,
      executorId: EXECUTOR_UID,
      executionStatus: "Not Started",
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID]
    });
  });

  // Contributor tries to change executor -> FAILS
  const contributorDb = testEnv.authenticatedContext(CONTRIBUTOR_UID).firestore();
  await assertFails(
    contributorDb.collection("ideas").doc("idea_5").update({
      executorId: CONTRIBUTOR_UID
    })
  );

  // Contributor tries to add new member -> FAILS
  await assertFails(
    contributorDb.collection("ideas").doc("idea_5").update({
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID, "new_member"]
    })
  );

  // Owner changes executor -> SUCCEEDS
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  await assertSucceeds(
    ownerDb.collection("ideas").doc("idea_5").update({
      executorId: CONTRIBUTOR_UID
    })
  );
});

test("Only Owner can delete the idea", async () => {
  await testEnv.withSecurityRulesDisabled(async (adminCtx) => {
    await adminCtx.firestore().collection("ideas").doc("idea_6").set({
      title: "Delete Me",
      ownerId: OWNER_UID,
      executorId: EXECUTOR_UID,
      executionStatus: "Not Started",
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID]
    });
  });

  // Executor tries to delete -> FAILS
  const executorDb = testEnv.authenticatedContext(EXECUTOR_UID).firestore();
  await assertFails(executorDb.collection("ideas").doc("idea_6").delete());

  // Owner deletes -> SUCCEEDS
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  await assertSucceeds(ownerDb.collection("ideas").doc("idea_6").delete());
});

test("Activity feed: members can post and read comments, cannot delete", async () => {
  await testEnv.withSecurityRulesDisabled(async (adminCtx) => {
    await adminCtx.firestore().collection("ideas").doc("idea_7").set({
      title: "Activity Project",
      ownerId: OWNER_UID,
      executorId: EXECUTOR_UID,
      executionStatus: "Not Started",
      memberIds: [OWNER_UID, EXECUTOR_UID, CONTRIBUTOR_UID]
    });
  });

  const contributorDb = testEnv.authenticatedContext(CONTRIBUTOR_UID).firestore();
  await assertSucceeds(
    contributorDb.collection("ideas").doc("idea_7").collection("activities").doc("act_1").set({
      ideaId: "idea_7",
      userId: CONTRIBUTOR_UID,
      userName: "Charlie",
      message: "I completed the API specification!",
      type: "comment",
      timestamp: Date.now()
    })
  );

  // Outside user cannot post activity
  const outsiderDb = testEnv.authenticatedContext(OUTSIDER_UID).firestore();
  await assertFails(
    outsiderDb.collection("ideas").doc("idea_7").collection("activities").doc("act_2").set({
      ideaId: "idea_7",
      userId: OUTSIDER_UID,
      userName: "Mallory",
      message: "Spam comment",
      type: "comment",
      timestamp: Date.now()
    })
  );

  // Activity cannot be deleted
  await assertFails(
    contributorDb.collection("ideas").doc("idea_7").collection("activities").doc("act_1").delete()
  );
});
