package dev.opencode.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.opencode.mobile.core.common.LambdaViewModelFactory
import dev.opencode.mobile.features.chat.ChatScreen
import dev.opencode.mobile.features.chat.ChatViewModel
import dev.opencode.mobile.features.files.FilesScreen
import dev.opencode.mobile.features.files.FilesViewModel
import dev.opencode.mobile.features.home.HomeScreen
import dev.opencode.mobile.features.projects.ProjectsScreen
import dev.opencode.mobile.features.projects.ProjectsViewModel
import dev.opencode.mobile.features.sessions.SessionListScreen
import dev.opencode.mobile.features.sessions.SessionListViewModel
import dev.opencode.mobile.features.settings.AddServerScreen
import dev.opencode.mobile.features.settings.AddServerViewModel
import dev.opencode.mobile.features.settings.ServerListScreen
import dev.opencode.mobile.features.settings.ServerListViewModel
import dev.opencode.mobile.opencode.repository.SessionRepository
import dev.opencode.mobile.ui.components.OpenCodeBottomBar
import dev.opencode.mobile.ui.theme.NavRoute
import dev.opencode.mobile.ui.theme.OpenCodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as OpenCodeApplication
        setContent {
            OpenCodeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    OpenCodeNavRoot(app)
                }
            }
        }
    }
}

@Composable
private fun OpenCodeNavRoot(app: OpenCodeApplication) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Session/message data needs a repository bound to the active server,
    // which is resolved asynchronously (Room + DataStore reads). Screens
    // below Home/Settings depend on it, so we resolve it once here and
    // recompute whenever the active server changes.
    var sessionRepository by remember { mutableStateOf<SessionRepository?>(null) }
    var repoLoading by remember { mutableStateOf(true) }
    val activeServerId by app.serverRepository.activeServerId.collectAsState(initial = null)

    LaunchedEffect(activeServerId) {
        repoLoading = true
        sessionRepository = app.sessionRepositoryForActiveServer()
        repoLoading = false
    }

    val showBottomBar = currentRoute in listOf(NavRoute.Home.route, NavRoute.Projects.route, NavRoute.Settings.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                OpenCodeBottomBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route.route) {
                        launchSingleTop = true
                        popUpTo(NavRoute.Home.route) { inclusive = false }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = NavRoute.Home.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(NavRoute.Home.route) {
                    val servers by app.serverRepository.observeServers().collectAsState(initial = emptyList())
                    HomeScreen(
                        servers = servers,
                        activeServerId = activeServerId,
                        onOpenProjects = { navController.navigate(NavRoute.Projects.route) },
                        onOpenSettings = { navController.navigate(NavRoute.Settings.route) },
                    )
                }

                composable(NavRoute.Projects.route) {
                    RequiresSessionRepository(repoLoading, sessionRepository) { repo ->
                        val vm: ProjectsViewModel = viewModel(
                            factory = LambdaViewModelFactory { ProjectsViewModel(repo.client) },
                        )
                        ProjectsScreen(
                            viewModel = vm,
                            onOpenSessions = { projectId ->
                                navController.navigate(NavRoute.Sessions.create(projectId))
                            },
                            onOpenFiles = { directory ->
                                navController.navigate(NavRoute.Files.create(directory))
                            },
                        )
                    }
                }

                composable(NavRoute.Files.route) { backStack ->
                    val encodedDirectory = backStack.arguments?.getString("directory") ?: return@composable
                    val directory = NavRoute.Files.decode(encodedDirectory)
                    RequiresSessionRepository(repoLoading, sessionRepository) { repo ->
                        val vm: FilesViewModel = viewModel(
                            factory = LambdaViewModelFactory { FilesViewModel(repo.client, directory) },
                        )
                        FilesScreen(viewModel = vm, onBack = { navController.popBackStack() })
                    }
                }

                composable(NavRoute.Sessions.route) { backStack ->
                    val projectId = backStack.arguments?.getString("projectId") ?: return@composable
                    RequiresSessionRepository(repoLoading, sessionRepository) { repo ->
                        val vm: SessionListViewModel = viewModel(
                            factory = LambdaViewModelFactory { SessionListViewModel(projectId, repo) },
                        )
                        SessionListScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                            onOpenSession = { sessionId -> navController.navigate(NavRoute.Chat.create(sessionId)) },
                        )
                    }
                }

                composable(NavRoute.Chat.route) { backStack ->
                    val sessionId = backStack.arguments?.getString("sessionId") ?: return@composable
                    RequiresSessionRepository(repoLoading, sessionRepository) { repo ->
                        val vm: ChatViewModel = viewModel(
                            factory = LambdaViewModelFactory { ChatViewModel(sessionId, repo) },
                        )
                        ChatScreen(viewModel = vm, onBack = { navController.popBackStack() })
                    }
                }

                composable(NavRoute.Settings.route) {
                    val vm: ServerListViewModel = viewModel(
                        factory = LambdaViewModelFactory { ServerListViewModel(app.serverRepository) },
                    )
                    ServerListScreen(
                        viewModel = vm,
                        onAddServer = { navController.navigate(NavRoute.AddServer.create()) },
                        onEditServer = { id -> navController.navigate(NavRoute.AddServer.create(id)) },
                    )
                }

                composable(NavRoute.AddServer.route) { backStack ->
                    val serverId = backStack.arguments?.getString("serverId")?.takeIf { it.isNotBlank() }
                    val vm: AddServerViewModel = viewModel(
                        factory = LambdaViewModelFactory { AddServerViewModel(app.serverRepository, serverId) },
                    )
                    AddServerScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

/** Gate for screens that need a [SessionRepository] bound to an active server. */
@Composable
private fun RequiresSessionRepository(
    loading: Boolean,
    repository: SessionRepository?,
    content: @Composable (SessionRepository) -> Unit,
) {
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        repository == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connect a server in Settings first")
        }
        else -> content(repository)
    }
}
