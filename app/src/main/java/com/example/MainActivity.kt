package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.IdeaMatrixBottomNavBar
import com.example.ui.components.NavDestination
import com.example.ui.screens.addidea.AddIdeaScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.ideas.IdeaDetailEditScreen
import com.example.ui.screens.ideas.IdeasScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.resources.ResourcesScreen
import com.example.ui.theme.IdeaMatrixTheme
import com.example.ui.viewmodel.IdeaViewModel
import com.example.ui.viewmodel.ThemeMode

import com.example.ui.screens.audit.AuditScreen
import com.example.ui.screens.auth.SignInScreen

class MainActivity : ComponentActivity() {
    private val viewModel: IdeaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemIsDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemIsDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            val currentUser by viewModel.currentUser.collectAsState()
            val isGuestMode by viewModel.isGuestMode.collectAsState()
            val isAuthLoading by viewModel.isAuthLoading.collectAsState()
            val authError by viewModel.authError.collectAsState()

            IdeaMatrixTheme(darkTheme = isDark) {
                if (currentUser == null && !isGuestMode) {
                    SignInScreen(
                        isLoading = isAuthLoading,
                        errorMessage = authError,
                        onSignInClick = { activity ->
                            viewModel.signInWithGoogle(activity)
                        },
                        onContinueWithoutAccount = {
                            viewModel.setGuestMode(true)
                        }
                    )
                } else {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: IdeaViewModel) {
    var selectedDestination by remember { mutableStateOf(NavDestination.HOME) }
    var isAddingIdea by remember { mutableStateOf(false) }
    val selectedIdeaForDetail by viewModel.selectedIdeaForDetail.collectAsState()
    val startInEditMode by viewModel.startInEditMode.collectAsState()

    // If on a secondary tab and user presses back, go back to Home
    BackHandler(enabled = selectedDestination != NavDestination.HOME && !isAddingIdea && selectedIdeaForDetail == null) {
        selectedDestination = NavDestination.HOME
    }

    if (selectedIdeaForDetail != null) {
        IdeaDetailEditScreen(
            idea = selectedIdeaForDetail!!,
            viewModel = viewModel,
            initialEditMode = startInEditMode,
            onDismiss = { viewModel.selectIdeaForDetail(null) }
        )
    } else if (isAddingIdea) {
        AddIdeaScreen(
            viewModel = viewModel,
            onDismiss = { isAddingIdea = false }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_scaffold"),
            bottomBar = {
                IdeaMatrixBottomNavBar(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { destination ->
                        selectedDestination = destination
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedDestination,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_navigation"
                ) { destination ->
                    when (destination) {
                        NavDestination.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToAddIdea = { isAddingIdea = true }
                        )

                        NavDestination.IDEAS -> IdeasScreen(
                            viewModel = viewModel,
                            onNavigateToAddIdea = { isAddingIdea = true }
                        )

                        NavDestination.AUDIT -> AuditScreen(
                            viewModel = viewModel,
                            onBack = { selectedDestination = NavDestination.HOME }
                        )

                        NavDestination.RESOURCES -> ResourcesScreen(
                            viewModel = viewModel
                        )

                        NavDestination.PROFILE -> ProfileScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
