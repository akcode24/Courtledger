package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import java.text.SimpleDateFormat
import java.util.*


private var lastToast: android.widget.Toast? = null

fun showSafeToast(context: android.content.Context, message: String, duration: Int = android.widget.Toast.LENGTH_SHORT) {
    try {
        lastToast?.cancel()
    } catch (e: Exception) {
        // Safe catch
    }
    try {
        val toast = android.widget.Toast.makeText(context.applicationContext, message, duration)
        lastToast = toast
        toast.show()
    } catch (e: Exception) {
        // Fallback
    }
}

data class AppColors(
    val bg: Color = Color(0xFFF7F9FB),
    val surface: Color = Color.White,
    val border: Color = Color(0xFFE1E2E5),
    val textPrimary: Color = Color(0xFF1A1C1E),
    val textSecondary: Color = Color(0xFF5F6368),
    val textInverse: Color = Color.White,
    val isDark: Boolean = false
)

val LocalAppColors = staticCompositionLocalOf { AppColors() }

class MainActivity : ComponentActivity() {
    private val viewModel: CourtLedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val appColors = if (isDarkTheme) {
                AppColors(
                    bg = Color(0xFF121314),
                    surface = Color(0xFF1E2022),
                    border = Color(0xFF2E3134),
                    textPrimary = Color(0xFFE3E2E6),
                    textSecondary = Color(0xFF909499),
                    textInverse = Color(0xFF121314),
                    isDark = true
                )
            } else {
                AppColors(
                    bg = Color(0xFFF7F9FB),
                    surface = Color.White,
                    border = Color(0xFFE1E2E5),
                    textPrimary = Color(0xFF1A1C1E),
                    textSecondary = Color(0xFF5F6368),
                    textInverse = Color.White,
                    isDark = false
                )
            }

            val view = androidx.compose.ui.platform.LocalView.current
            if (!view.isInEditMode) {
                androidx.compose.runtime.SideEffect {
                    val context = view.context
                    var currentContext = context
                    var activity: android.app.Activity? = null
                    while (currentContext is android.content.ContextWrapper) {
                        if (currentContext is android.app.Activity) {
                            activity = currentContext
                            break
                        }
                        currentContext = currentContext.baseContext
                    }
                    if (activity == null && context is android.app.Activity) {
                        activity = context
                    }
                    val window = activity?.window
                    if (window != null) {
                        window.statusBarColor = (if (isDarkTheme) 0xFF1E2022 else 0xFFFFFFFF).toInt()
                        window.navigationBarColor = (if (isDarkTheme) 0xFF1E2022 else 0xFFFFFFFF).toInt()
                        androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
                        androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDarkTheme
                    }
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalAppColors provides appColors) {
                    CourtLedgerApp(viewModel)
                }
            }
        }
    }
}

// Sport Theme helpers mapping to unique UI colors
data class SportTheme(
    val primary: Color,
    val darkGradient: Brush,
    val lightContainer: Color,
    val darkContainer: Color,
    val textOnPrimary: Color = Color(0xFF1D1B20)
)

fun getSportTheme(sportName: String, themeSetting: String): SportTheme {
    return when (themeSetting) {
        "Teal" -> SportTheme(
            primary = Color(0xFF00796B),
            darkGradient = Brush.verticalGradient(listOf(Color(0xFFE0F2F1), Color(0xFFC7EBE7))),
            lightContainer = Color(0xFFE0F2F1),
            darkContainer = Color(0xFF004D40),
            textOnPrimary = Color(0xFF003024)
        )
        "Emerald" -> SportTheme(
            primary = Color(0xFF2E7D32),
            darkGradient = Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFCBEAD0))),
            lightContainer = Color(0xFFE8F5E9),
            darkContainer = Color(0xFF1B5E20),
            textOnPrimary = Color(0xFF0A3C10)
        )
        "Amber" -> SportTheme(
            primary = Color(0xFFD84315),
            darkGradient = Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE5B9))),
            lightContainer = Color(0xFFFFF3E0),
            darkContainer = Color(0xFFE65100),
            textOnPrimary = Color(0xFF5D2100)
        )
        "Indigo" -> SportTheme(
            primary = Color(0xFF0061A4),
            darkGradient = Brush.verticalGradient(listOf(Color(0xFFD3E3FD), Color(0xFFC2D9FA))),
            lightContainer = Color(0xFFD3E3FD),
            darkContainer = Color(0xFF041E49),
            textOnPrimary = Color(0xFF041E49)
        )
        "Crimson" -> SportTheme(
            primary = Color(0xFFC62828),
            darkGradient = Brush.verticalGradient(listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))),
            lightContainer = Color(0xFFFFEBEE),
            darkContainer = Color(0xFF880E4F),
            textOnPrimary = Color(0xFF32031B)
        )
        else -> { // Default matching sport category
            when (sportName.lowercase()) {
                "badminton" -> SportTheme(
                    primary = Color(0xFF0061A4),
                    darkGradient = Brush.verticalGradient(listOf(Color(0xFFD3E3FD), Color(0xFFC2D9FA))),
                    lightContainer = Color(0xFFD3E3FD),
                    darkContainer = Color(0xFF041E49),
                    textOnPrimary = Color(0xFF041E49)
                )
                "football" -> SportTheme(
                    primary = Color(0xFF2E7D32),
                    darkGradient = Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFCBEAD0))),
                    lightContainer = Color(0xFFE8F5E9),
                    darkContainer = Color(0xFF1B5E20),
                    textOnPrimary = Color(0xFF0A3C10)
                )
                "tennis" -> SportTheme(
                    primary = Color(0xFFD84315),
                    darkGradient = Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE5B9))),
                    lightContainer = Color(0xFFFFF3E0),
                    darkContainer = Color(0xFFE65100),
                    textOnPrimary = Color(0xFF5D2100)
                )
                else -> SportTheme(
                    primary = Color(0xFF673AB7),
                    darkGradient = Brush.verticalGradient(listOf(Color(0xFFEDE7F6), Color(0xFFE1D5F2))),
                    lightContainer = Color(0xFFEDE7F6),
                    darkContainer = Color(0xFF3F218C),
                    textOnPrimary = Color(0xFF1E004B)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourtLedgerApp(viewModel: CourtLedgerViewModel) {
    val groups by viewModel.allGroups.collectAsStateWithLifecycle()
    val activeGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    val emailLogs by viewModel.emailLogs.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var activeTab by remember { mutableStateOf("dashboard") } // "dashboard", "sessions", "payments"

    // Dialog trigger states
    var showAddGroup by remember { mutableStateOf(false) }
    var showAddMember by remember { mutableStateOf(false) }
    var showAddSession by remember { mutableStateOf(false) }
    var showAddPayment by remember { mutableStateOf(false) }
    var showAddBulkCost by remember { mutableStateOf(false) }
    var showEditBulkCost by remember { mutableStateOf<BulkExpense?>(null) }
    var showEmailLogs by remember { mutableStateOf(false) }

    // Settle player selection states
    var settlePlayerSelector by remember { mutableStateOf<MemberBalance?>(null) }
    var explicitSelectedMemberId by remember { mutableStateOf<Int?>(null) }
    var explicitSelectedAmount by remember { mutableStateOf<Double?>(null) }

    // Selected sport theme
    val sportTheme = getSportTheme(
        sportName = state.group?.sportType ?: "",
        themeSetting = state.group?.themeColor ?: "Teal"
    )

    val currentBorder = LocalAppColors.current.border

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LocalAppColors.current.bg,
        bottomBar = {
            NavigationBar(
                containerColor = LocalAppColors.current.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = currentBorder,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { activeTab = "dashboard" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = sportTheme.primary,
                        selectedTextColor = sportTheme.primary,
                        indicatorColor = sportTheme.primary.copy(alpha = 0.15f),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = activeTab == "sessions",
                    onClick = { activeTab = "sessions" },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Play Sessions") },
                    label = { Text("Sessions") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = sportTheme.primary,
                        selectedTextColor = sportTheme.primary,
                        indicatorColor = sportTheme.primary.copy(alpha = 0.15f),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    ),
                    modifier = Modifier.testTag("nav_sessions")
                )
                NavigationBarItem(
                    selected = activeTab == "payments",
                    onClick = { activeTab = "payments" },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Payments") },
                    label = { Text("Payments") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = sportTheme.primary,
                        selectedTextColor = sportTheme.primary,
                        indicatorColor = sportTheme.primary.copy(alpha = 0.15f),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    ),
                    modifier = Modifier.testTag("nav_payments")
                )
                NavigationBarItem(
                    selected = activeTab == "settings",
                    onClick = { activeTab = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = sportTheme.primary,
                        selectedTextColor = sportTheme.primary,
                        indicatorColor = sportTheme.primary.copy(alpha = 0.15f),
                        unselectedIconColor = Color(0xFF5F6368),
                        unselectedTextColor = Color(0xFF5F6368)
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LocalAppColors.current.surface)
                        .drawBehind {
                            drawLine(
                                color = currentBorder,
                                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title Logo Brand
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFE0F2F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.shuttlecock_vector),
                                    contentDescription = "CourtLedger Brand Icon",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CourtLedger",
                                    color = LocalAppColors.current.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = state.group?.sportType ?: "Cost Splitting Screen",
                                    color = LocalAppColors.current.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Group list quick picker dropdown
                        var groupMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { groupMenuExpanded = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                                    contentColor = LocalAppColors.current.textPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("group_selector_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = state.group?.name ?: "No Group",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 100.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = groupMenuExpanded,
                                onDismissRequest = { groupMenuExpanded = false },
                                modifier = Modifier.background(LocalAppColors.current.surface)
                            ) {
                                groups.forEach { grp ->
                                    DropdownMenuItem(
                                        text = { Text(grp.name, color = LocalAppColors.current.textPrimary) },
                                        onClick = {
                                            viewModel.selectGroup(grp.id)
                                            groupMenuExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = grp.sportType,
                                                tint = getSportTheme(grp.sportType, grp.themeColor).primary,
                                                modifier = Modifier.size(16.dp)
                                              )
                                        }
                                    )
                                }
                                HorizontalDivider(color = LocalAppColors.current.border)
                                DropdownMenuItem(
                                    text = { Text("+ Create Group", color = sportTheme.primary) },
                                    onClick = {
                                        showAddGroup = true
                                        groupMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Main body according to activeTab
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { tab ->
                    when (tab) {
                        "dashboard" -> DashboardView(
                            state = state,
                            sportTheme = sportTheme,
                            onAddMember = { showAddMember = true },
                            onLogPayment = { showAddPayment = true },
                            onAddBankTransaction = { desc, amt -> viewModel.addManualBankTransaction(desc, amt) },
                            onMemberClick = { settlePlayerSelector = it }
                        )
                        "sessions" -> SessionsView(
                            state = state,
                            sportTheme = sportTheme,
                            onLogSession = { showAddSession = true },
                            onDeleteSession = { viewModel.deleteSession(it) }
                        )
                        "payments" -> PaymentsView(
                            state = state,
                            sportTheme = sportTheme,
                            onLogPayment = { showAddPayment = true },
                            onLogBulkCost = { showAddBulkCost = true },
                            onDeletePayment = { viewModel.deletePayment(it) },
                            onDeleteBulkCost = { viewModel.deleteBulkExpense(it) },
                            onEditBulkCost = { showEditBulkCost = it }
                        )
                        "settings" -> SettingsView(
                            state = state,
                            sportTheme = sportTheme,
                            themeMode = themeMode,
                            onUpdateThemeMode = { viewModel.setThemeMode(it) },
                            onUpdateGroup = { name, sport, currency, theme ->
                                state.group?.let { currentGrp ->
                                    viewModel.updateGroup(
                                        currentGrp.copy(
                                            name = name,
                                            sportType = sport,
                                            currency = currency,
                                            themeColor = theme
                                        )
                                    )
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }

            // High Fidelity Modals / Overlays placed on top
            if (showAddGroup) {
                AddGroupOverlay(
                    sportTheme = sportTheme,
                    onDismiss = { showAddGroup = false },
                    onConfirm = { name, sport, currency, theme ->
                        viewModel.addGroup(name, sport, currency, theme)
                        showAddGroup = false
                    }
                )
            }

            if (showAddMember) {
                AddMemberOverlay(
                    sportTheme = sportTheme,
                    onDismiss = { showAddMember = false },
                    onConfirm = { name, role, email ->
                        viewModel.addMember(name, role, email)
                        showAddMember = false
                    }
                )
            }

            if (showAddPayment) {
                AddPaymentOverlay(
                    state = state,
                    sportTheme = sportTheme,
                    initialMemberId = explicitSelectedMemberId,
                    initialAmount = explicitSelectedAmount,
                    onDismiss = {
                        showAddPayment = false
                        explicitSelectedMemberId = null
                        explicitSelectedAmount = null
                    },
                    onConfirm = { memberId, amount, notes, treatAsMinusBulk ->
                        viewModel.addGuestPaymentWithReduction(memberId, amount, notes, treatAsMinusBulk)
                        showAddPayment = false
                        explicitSelectedMemberId = null
                        explicitSelectedAmount = null
                    }
                )
            }

            settlePlayerSelector?.let { selectedBalance ->
                SettlePlayerOverlay(
                    memberBalance = selectedBalance,
                    state = state,
                    sportTheme = sportTheme,
                    onDismiss = { settlePlayerSelector = null },
                    onInstantSettle = { confirmedAmount ->
                        viewModel.addPayment(selectedBalance.member.id, confirmedAmount, "Settled automatically")
                        settlePlayerSelector = null
                        showSafeToast(context, "Outstanding balance for ${selectedBalance.member.name} settled successfully!", android.widget.Toast.LENGTH_SHORT)
                    },
                    onCustomSettle = {
                        explicitSelectedMemberId = selectedBalance.member.id
                        explicitSelectedAmount = if (selectedBalance.outstandingBalance > 0.0) selectedBalance.outstandingBalance else null
                        settlePlayerSelector = null
                        showAddPayment = true
                    },
                    onDeletePayment = { payment ->
                        viewModel.deletePayment(payment)
                        // Refresh outstanding balance state reference if selector is still active, or simply let data stream emit
                    },
                    onUpdateMember = { updatedMember ->
                        viewModel.updateMember(updatedMember)
                        // Instantly update local selector state so changes reflect immediately in current overlay
                        settlePlayerSelector = selectedBalance.copy(member = updatedMember)
                    }
                )
            }

            if (showAddBulkCost) {
                AddBulkCostOverlay(
                    state = state,
                    sportTheme = sportTheme,
                    onDismiss = { showAddBulkCost = false },
                    onConfirm = { title, amount, paidByMemberId, isPaidFromBank ->
                        viewModel.addBulkExpense(title, amount, paidByMemberId, isPaidFromBank)
                        showAddBulkCost = false
                    }
                )
            }

            showEditBulkCost?.let { currentBulkCost ->
                EditBulkCostOverlay(
                    bulkExpense = currentBulkCost,
                    state = state,
                    sportTheme = sportTheme,
                    onDismiss = { showEditBulkCost = null },
                    onConfirm = { updatedBulkExpense ->
                        viewModel.updateBulkExpense(updatedBulkExpense)
                        showEditBulkCost = null
                    }
                )
            }

            if (showAddSession) {
                AddSessionOverlay(
                    state = state,
                    sportTheme = sportTheme,
                    onDismiss = { showAddSession = false },
                    onConfirm = { notes, dateMillis, expenses, attendanceList, isPaidFromBank ->
                        viewModel.addSession(notes, dateMillis, expenses, attendanceList, isPaidFromBank)
                        showAddSession = false
                    }
                )
            }

            if (showEmailLogs) {
                EmailLogsOverlay(
                    logs = emailLogs,
                    sportTheme = sportTheme,
                    onDismiss = { showEmailLogs = false },
                    onClear = { viewModel.clearEmailLogs() }
                )
            }
        }
    }
}

// ----------------------------------------------------
// UI TABS IMPLEMENTATION
// ----------------------------------------------------

@Composable
fun DashboardViewModelCard(
    state: GroupDashboardState,
    sportTheme: SportTheme
) {
    val currency = state.group?.currency ?: "$"
    val userBalance = state.balances.find { it.member.id == state.currentUser?.id }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .background(sportTheme.darkGradient)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.group?.name?.uppercase(Locale.getDefault()) ?: "COURTLEDGER ACTIVE",
                        color = sportTheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(sportTheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = state.group?.sportType ?: "Sport",
                            color = sportTheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (userBalance != null) {
                    val bal = userBalance.outstandingBalance
                    Text(
                        text = if (bal >= 0) "Total amount you owe" else "Total amount owed to you",
                        color = sportTheme.textOnPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$currency${String.format("%.2f", Math.abs(bal))}",
                        color = if (bal > 0) Color(0xFFB3261E) else if (bal < 0) Color(0xFF137333) else sportTheme.textOnPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active user: ${userBalance.member.name} (${userBalance.member.role})",
                        color = sportTheme.textOnPrimary.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "No Members Configured Yet",
                        color = sportTheme.textOnPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add some group members below to get started!",
                        color = sportTheme.textOnPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

data class TransactionTransfer(
    val fromMemberName: String,
    val toMemberName: String,
    val amount: Double
)

class DebtNode(val name: String, var amount: Double)

fun calculateSimplifiedTransfers(balances: List<MemberBalance>): List<TransactionTransfer> {
    val debtors = balances.filter { it.outstandingBalance > 0.01 }
        .map { DebtNode(it.member.name, it.outstandingBalance) }
        .sortedByDescending { it.amount }
        
    val creditors = balances.filter { it.outstandingBalance < -0.01 }
        .map { DebtNode(it.member.name, -it.outstandingBalance) }
        .sortedByDescending { it.amount }
        
    val transfers = mutableListOf<TransactionTransfer>()
    
    var dIdx = 0
    var cIdx = 0
    
    while (dIdx < debtors.size && cIdx < creditors.size) {
        val debtor = debtors[dIdx]
        val creditor = creditors[cIdx]
        
        if (debtor.amount < 0.01) {
            dIdx++
            continue
        }
        if (creditor.amount < 0.01) {
            cIdx++
            continue
        }
        
        val payment = Math.min(debtor.amount, creditor.amount)
        transfers.add(TransactionTransfer(debtor.name, creditor.name, payment))
        
        debtor.amount -= payment
        creditor.amount -= payment
        
        if (debtor.amount < 0.01) {
            dIdx++
        }
        if (creditor.amount < 0.01) {
            cIdx++
        }
    }
    return transfers
}

@Composable
fun DashboardView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onAddMember: () -> Unit,
    onLogPayment: () -> Unit,
    onAddBankTransaction: (String, Double) -> Unit,
    onMemberClick: (MemberBalance) -> Unit
) {
    var dashboardSubTab by remember { mutableStateOf("balances") } // "balances", "bank", "insights"
    var showManualDepositDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("deposit") } // "deposit" or "withdrawal"

    val sortedBalances = remember(state.balances) {
        state.balances.sortedWith(
            compareBy<MemberBalance> { it.member.role == "One-time Player" }
                .thenBy { it.member.name.lowercase(Locale.ROOT) }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            DashboardViewModelCard(state, sportTheme)
        }

        // Action Quick Commands Panel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // If Administrator: Show full action suite
                val isAdmin = state.currentUser?.role == "Admin"

                Button(
                    onClick = onLogPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("quick_payment_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay / Settle", fontSize = 12.sp)
                    }
                }

                if (isAdmin) {
                    Button(
                        onClick = onAddMember,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFE1E2E5),
                            contentColor = LocalAppColors.current.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("quick_add_member_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = LocalAppColors.current.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Member", fontSize = 12.sp, color = LocalAppColors.current.textPrimary)
                        }
                    }
                }
            }
        }

        // Dashboard Sub-Tab Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF1F3F4), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val subTabsByIcon = listOf(
                    Triple("balances", "Ledger", Icons.Default.List),
                    Triple("bank", "Treasury", Icons.Default.AccountBalance),
                    Triple("insights", "Analytics", Icons.Default.Info)
                )
                subTabsByIcon.forEach { (key, label, iconVec) ->
                    val isSelected = dashboardSubTab == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LocalAppColors.current.surface else Color.Transparent)
                            .clickable { dashboardSubTab = key }
                            .padding(vertical = 8.dp)
                            .testTag("sub_tab_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = null,
                                tint = if (isSelected) sportTheme.primary else LocalAppColors.current.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                color = if (isSelected) sportTheme.primary else LocalAppColors.current.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        if (dashboardSubTab == "balances") {
            // Member balances roster header with WhatsApp dues quick copy button
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Player Ledger Balances",
                            color = LocalAppColors.current.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${state.members.size} members",
                            color = LocalAppColors.current.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (state.balances.isNotEmpty()) {
                        Button(
                            onClick = {
                                val msg = buildString {
                                    appendLine("📢 *${state.group?.name ?: "Group"} Balance Update*")
                                    appendLine("Sport: *${state.group?.sportType ?: "Other"}*")
                                    appendLine("Generated: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}")
                                    appendLine("--------------------------------")
                                    
                                    val owes = state.balances.filter { it.outstandingBalance > 0 }
                                    val owedBack = state.balances.filter { it.outstandingBalance < 0 }
                                    val fullySettled = state.balances.filter { it.outstandingBalance == 0.0 }
                                    val currencySymbol = state.group?.currency ?: "$"

                                    if (owes.isNotEmpty()) {
                                        appendLine("*⚠️ Pending Payments (Owes Admin):*")
                                        owes.forEach { b ->
                                            appendLine("• *${b.member.name}*: $currencySymbol${String.format(Locale.ROOT, "%.2f", b.outstandingBalance)}")
                                        }
                                        appendLine()
                                    }

                                    if (owedBack.isNotEmpty()) {
                                        appendLine("*✅ Owed Back:*")
                                        owedBack.forEach { b ->
                                            appendLine("• *${b.member.name}*: $currencySymbol${String.format(Locale.ROOT, "%.2f", Math.abs(b.outstandingBalance))}")
                                        }
                                        appendLine()
                                    }

                                    if (fullySettled.isNotEmpty()) {
                                        appendLine("*🎉 Fully Settled:*")
                                        appendLine(fullySettled.joinToString(", ") { it.member.name })
                                        appendLine()
                                    }
                                    
                                    appendLine("Please clear your dues with Admin. Thank you! 🙏")
                                }
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg))
                                showSafeToast(context, "WhatsApp digest copied to clipboard!", android.widget.Toast.LENGTH_SHORT)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).testTag("whatsapp_copy_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Copy WhatsApp digest", tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy for WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (state.balances.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No players. Use buttons to customize members!", color = LocalAppColors.current.textSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                items(sortedBalances) { b ->
                    val currency = state.group?.currency ?: "$"
                    val isMe = b.member.id == state.currentUser?.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LocalAppColors.current.surface)
                            .border(
                                1.dp,
                                if (isMe) sportTheme.primary.copy(alpha = 0.5f) else LocalAppColors.current.border,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onMemberClick(b) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar circles
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (b.member.role == "Admin") sportTheme.primary.copy(alpha = 0.15f) else (if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = b.member.name.take(2).uppercase(Locale.ROOT),
                                    color = if (b.member.role == "Admin") sportTheme.primary else LocalAppColors.current.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = b.member.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalAppColors.current.textPrimary
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(You)",
                                            color = sportTheme.primary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (b.member.role == "One-time Player") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (LocalAppColors.current.isDark) Color(0xFF3E361A) else Color(0xFFFFF3CD))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "GUEST",
                                                color = if (LocalAppColors.current.isDark) Color(0xFFF6C343) else Color(0xFF856404),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${b.member.role} • ${b.member.email}",
                                    color = LocalAppColors.current.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val bal = b.outstandingBalance
                            Text(
                                text = if (bal > 0) "owes admin" else if (bal < 0) "owed back" else "all settled",
                                color = LocalAppColors.current.textSecondary,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (bal == 0.0) "${currency}0.00" else "$currency${String.format("%.2f", Math.abs(bal))}",
                                color = if (bal > 0) (if (LocalAppColors.current.isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)) else if (bal < 0) (if (LocalAppColors.current.isDark) Color(0xFF81C784) else Color(0xFF137333)) else LocalAppColors.current.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        if (dashboardSubTab == "bank") {
            val currency = state.group?.currency ?: "$"

            // Feature Header and Explainer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Group Treasury Bank",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textPrimary
                    )
                    Text(
                        text = "A joint ledger account for excess player overpayments and common sports resources. Session expenses can be funded directly from this bank balance.",
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Balance Display Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = sportTheme.primary.copy(alpha = if (LocalAppColors.current.isDark) 0.15f else 0.08f)
                    ),
                    border = BorderStroke(1.dp, sportTheme.primary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL BANK BALANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = sportTheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currency${String.format("%.2f", state.bankBalance)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LocalAppColors.current.textPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons for Bank Contributions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    transactionType = "deposit"
                                    showManualDepositDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = sportTheme.primary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("bank_deposit_button"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Manual Deposit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Deposit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    transactionType = "withdrawal"
                                    showManualDepositDialog = true
                                },
                                border = BorderStroke(1.dp, LocalAppColors.current.border),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = LocalAppColors.current.textPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("bank_withdrawal_button"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Manual Extraction",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Deduction", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Transactions Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Transaction Ledger History",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textPrimary
                    )
                    Text(
                        text = "Ledger list of all additions and subtractions from the group treasury",
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (state.bankTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                if (LocalAppColors.current.isDark) Color(0xFF1E2022) else Color(0xFFF7F8FA),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Bank template icon",
                                tint = LocalAppColors.current.textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No bank activity yet",
                                color = LocalAppColors.current.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(state.bankTransactions.sortedByDescending { it.dateMillis }) { tx ->
                    val isAddition = tx.amount >= 0.0
                    val absVal = Math.abs(tx.amount)
                    val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(tx.dateMillis))

                    val itemBg = if (LocalAppColors.current.isDark) Color(0xFF1F2225) else Color(0xFFF7F8FA)
                    val greenTint = if (LocalAppColors.current.isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                    val greenBg = if (LocalAppColors.current.isDark) Color(0xFF1E3523) else Color(0xFFE8F5E9)
                    val redTint = if (LocalAppColors.current.isDark) Color(0xFFE57373) else Color(0xFFC62828)
                    val redBg = if (LocalAppColors.current.isDark) Color(0xFF3C2022) else Color(0xFFFFEBEE)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(itemBg, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, LocalAppColors.current.border, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isAddition) greenBg else redBg,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAddition) Icons.Default.CheckCircle else Icons.Default.AccountBalance,
                                    contentDescription = "Transaction Logo",
                                    tint = if (isAddition) greenTint else redTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.description,
                                        color = LocalAppColors.current.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (tx.isSystemOverpayment) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(sportTheme.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Overfill", fontSize = 8.sp, color = sportTheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateStr,
                                    color = LocalAppColors.current.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "${if (isAddition) "+" else "-"} $currency${String.format("%.2f", absVal)}",
                            color = if (isAddition) greenTint else redTint,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        if (dashboardSubTab == "insights") {
            val sessionsCount = state.sessions.size.coerceAtLeast(1)
            val totalSessionCost = state.sessions.sumOf { it.totalCost }
            val avgSessionCost = totalSessionCost / sessionsCount
            val totalBulkCost = state.bulkExpenses.sumOf { it.amount }
            val totalOverallSpend = totalSessionCost + totalBulkCost
            val currency = state.group?.currency ?: "$"

            // Attendance counts per member
            val attendeeCounts = mutableMapOf<Int, Int>() 
            state.sessionDetails.forEach { sd ->
                sd.attendees.forEach { member ->
                    attendeeCounts[member.id] = (attendeeCounts[member.id] ?: 0) + 1
                }
            }

            // Leaderboard sorted by sessions attended
            val attendanceLeaderboard = state.members.map { m ->
                val count = attendeeCounts[m.id] ?: 0
                val percentage = if (state.sessions.isNotEmpty()) {
                    (count.toDouble() / state.sessions.size * 100).toInt()
                } else {
                    0
                }
                m to Pair(count, percentage)
            }.sortedByDescending { it.second.first }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Group Ledger Performance & Analytics",
                        color = Color(0xFF1A1C1E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Metrics computed from all logged transactions",
                        color = Color(0xFF5F6368),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Main Overall metrics box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = sportTheme.primary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, sportTheme.primary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("TOTAL CUMULATIVE GROUP BUDGET SPEND", color = Color(0xFF5F6368), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$currency${String.format("%.2f", totalOverallSpend)}", color = sportTheme.primary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Court / Session Sessions", color = Color.Gray, fontSize = 9.sp)
                                    Text("$currency${String.format("%.2f", totalSessionCost)}", color = Color(0xFF1A1C1E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("General Bulk Purchases", color = Color.Gray, fontSize = 9.sp)
                                    Text("$currency${String.format("%.2f", totalBulkCost)}", color = Color(0xFF1A1C1E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quick stats indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: Session average
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                            border = BorderStroke(1.dp, LocalAppColors.current.border),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = sportTheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Avg Session Cost", color = LocalAppColors.current.textSecondary, fontSize = 9.sp)
                                Text("$currency${String.format("%.2f", avgSessionCost)}", color = LocalAppColors.current.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Stat 2: Sessions count
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                            border = BorderStroke(1.dp, LocalAppColors.current.border),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Total Sessions", color = LocalAppColors.current.textSecondary, fontSize = 9.sp)
                                Text("${state.sessions.size} sessions", color = LocalAppColors.current.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Stat 3: Registered members
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                            border = BorderStroke(1.dp, LocalAppColors.current.border),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Active Players", color = LocalAppColors.current.textSecondary, fontSize = 9.sp)
                                Text("${state.members.size} players", color = LocalAppColors.current.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Attendance League leaderboards
                    Text(
                        text = "🏆 Attendance Leaderboard & MVP Rankings",
                        color = Color(0xFF1A1C1E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Who plays most frequently? Sorted by attended games percentage.",
                        color = Color(0xFF5F6368),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            if (attendanceLeaderboard.isEmpty()) {
                item {
                    Text("No player attendance recorded. Play sessions will drive this table!", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(attendanceLeaderboard) { (m, stats) ->
                    val (count, percentage) = stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LocalAppColors.current.surface)
                            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "MVP star", tint = if (percentage >= 80) Color(0xFFFFB300) else Color.LightGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(m.name, color = LocalAppColors.current.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${m.role}", color = LocalAppColors.current.textSecondary, fontSize = 10.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("$count / ${state.sessions.size} games", color = LocalAppColors.current.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("$percentage% attendance", color = sportTheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    if (showManualDepositDialog) {
        var txDesc by remember { mutableStateOf("") }
        var txAmt by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showManualDepositDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                border = BorderStroke(1.dp, LocalAppColors.current.border),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (transactionType == "deposit") "Record Manual Contribution" else "Record Manual Deduction",
                        color = LocalAppColors.current.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Manually record a cash movement on behalf of the group ledger.",
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = txDesc,
                        onValueChange = { txDesc = it },
                        label = { Text("Reason / Description") },
                        placeholder = { Text(if (transactionType == "deposit") "e.g. Bank interest refund, cash donation" else "e.g., Equipment repair cash out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalAppColors.current.textPrimary,
                            unfocusedTextColor = LocalAppColors.current.textPrimary,
                            focusedLabelColor = sportTheme.primary,
                            focusedBorderColor = sportTheme.primary,
                            unfocusedLabelColor = LocalAppColors.current.textSecondary,
                            unfocusedBorderColor = LocalAppColors.current.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_tx_desc")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = txAmt,
                        onValueChange = { txAmt = it },
                        label = { Text("Cost Amount") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalAppColors.current.textPrimary,
                            unfocusedTextColor = LocalAppColors.current.textPrimary,
                            focusedLabelColor = sportTheme.primary,
                            focusedBorderColor = sportTheme.primary,
                            unfocusedLabelColor = LocalAppColors.current.textSecondary,
                            unfocusedBorderColor = LocalAppColors.current.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_tx_amount")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showManualDepositDialog = false }) { Text("Cancel", color = Color.Gray) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val amtDouble = txAmt.toDoubleOrNull()
                                if (txDesc.isNotBlank() && amtDouble != null && amtDouble > 0) {
                                    val finalAmt = if (transactionType == "deposit") amtDouble else -amtDouble
                                    onAddBankTransaction(txDesc, finalAmt)
                                    showManualDepositDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = sportTheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("bank_tx_submit")
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionsView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onLogSession: () -> Unit,
    onDeleteSession: (Session) -> Unit
) {
    val isAdmin = state.currentUser?.role == "Admin"
    var sessionsSubTab by remember { mutableStateOf("history") } // "history", "forecaster"

    var estCourtCostText by remember { mutableStateOf("40.00") }
    var estConsumablesText by remember { mutableStateOf("10.00") }
    
    val defaultMemberCount = remember(state.members) { 
        state.members.filter { it.role != "One-time Player" }.size.coerceAtLeast(1) 
    }
    var numMembers by remember(defaultMemberCount) { mutableStateOf(defaultMemberCount) }
    var numGuests by remember { mutableStateOf(2) }
    
    var guestPolicy by remember { mutableStateOf("equal") } // "equal", "fixed", "premium"
    var fixedGuestFeeText by remember { mutableStateOf("10.00") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Group Play Sessions", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Attendance records & cost logs", color = Color(0xFF5F6368), fontSize = 12.sp)
            }

            if (isAdmin && sessionsSubTab == "history") {
                Button(
                    onClick = onLogSession,
                    colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("log_session_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Session", fontSize = 12.sp)
                }
            }
        }

        // Sub-Tab Switcher for Sessions tab
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFFF1F3F4), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subTabs = listOf(
                "history" to "Session History",
                "forecaster" to "Play Cost Forecaster"
            )
            subTabs.forEach { (key, label) ->
                val isSelected = sessionsSubTab == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { sessionsSubTab = key }
                        .padding(vertical = 8.dp)
                        .testTag("sessions_sub_tab_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) sportTheme.primary else Color(0xFF5F6368),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (sessionsSubTab == "history") {
            if (state.sessionDetails.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No played sessions recorded yet.", color = Color(0xFF5F6368), fontSize = 14.sp)
                        if (isAdmin) {
                            Text("Log a session with expenses to test cost splitting!", color = Color(0xFF5F6368), fontSize = 12.sp)
                        } else {
                            Text("Admin must write session logs.", color = Color(0xFF5F6368), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    items(state.sessionDetails) { entry ->
                        val currency = state.group?.currency ?: "$"
                        val formattedDate = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(entry.session.dateMillis))

                        var isExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { isExpanded = !isExpanded },
                            colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                            border = BorderStroke(1.dp, LocalAppColors.current.border),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formattedDate,
                                            color = LocalAppColors.current.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = entry.session.notes.ifBlank { "Generic Sports Play" },
                                            color = LocalAppColors.current.textSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$currency${String.format("%.2f", entry.session.totalCost)}",
                                            color = sportTheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            modifier = Modifier.testTag("session_cost_amount")
                                        )
                                        Text(
                                            text = "${entry.attendees.size} Attended",
                                            color = Color(0xFF5F6368),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE1E2E5))

                                    // List of specific expenses in the session
                                    if (entry.expenseItems.isNotEmpty()) {
                                        Text("Cost Breakdown:", color = Color(0xFF5F6368), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        entry.expenseItems.forEach { exp ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(exp.name, color = Color(0xFF1A1C1E), fontSize = 12.sp)
                                                Text("$currency${String.format("%.2f", exp.amount)}", color = Color(0xFF5F6368), fontSize = 12.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // List of attendance splits
                                    Text("Calculated Splits:", color = Color(0xFF5F6368), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    state.members.forEach { m ->
                                        val owedShare = entry.splits[m.id] ?: 0.0
                                        val isPresent = entry.attendees.any { it.id == m.id }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isPresent) sportTheme.primary else Color(0xFFB0B3B8))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = m.name,
                                                    color = if (isPresent) Color(0xFF1A1C1E) else Color(0xFF5F6368),
                                                    fontSize = 13.sp
                                                )
                                                if (!isPresent) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("(Absent)", color = Color(0xFF5F6368), fontSize = 10.sp)
                                                }
                                            }

                                            Text(
                                                text = if (owedShare > 0) "$currency${String.format("%.2f", owedShare)}" else "${currency}0.00",
                                                color = if (owedShare > 0) sportTheme.primary else Color(0xFF5F6368),
                                                fontSize = 13.sp,
                                                fontWeight = if (owedShare > 0) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }

                                    // Danger Zone
                                    if (isAdmin) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = { onDeleteSession(entry.session) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Delete Logs", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Tap to view full splits & cost breakdown...",
                                        color = Color(0xFF5F6368),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Live Interactive Sandbox Costs Split Estimator
            val courtVal = estCourtCostText.toDoubleOrNull() ?: 0.0
            val consVal = estConsumablesText.toDoubleOrNull() ?: 0.0
            val totalCost = courtVal + consVal
            
            var memberCost = 0.0
            var guestCost = 0.0
            
            when (guestPolicy) {
                "equal" -> {
                    val totalPeople = (numMembers + numGuests).coerceAtLeast(1)
                    val share = totalCost / totalPeople
                    memberCost = share
                    guestCost = share
                }
                "fixed" -> {
                    val guestFee = fixedGuestFeeText.toDoubleOrNull() ?: 0.0
                    val guestContr = guestFee * numGuests
                    val remaining = (totalCost - guestContr).coerceAtLeast(0.0)
                    memberCost = if (numMembers > 0) remaining / numMembers else 0.0
                    guestCost = guestFee
                }
                "premium" -> {
                    val factor = numMembers + (1.5 * numGuests)
                    val baseShare = if (factor > 0) totalCost / factor else 0.0
                    memberCost = baseShare
                    guestCost = 1.5 * baseShare
                }
            }

            val currencySymbol = state.group?.currency ?: "$"

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Estimate game variables below to calculate precise turnout pricing splits before logging the real session. Copy are quote instantly for your WhatsApp roster!",
                        color = Color(0xFF5F6368),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Estimated Play Expenses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = estCourtCostText,
                                    onValueChange = { estCourtCostText = it },
                                    label = { Text("Court Fee") },
                                    prefix = { Text(currencySymbol) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("est_court_cost_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = estConsumablesText,
                                    onValueChange = { estConsumablesText = it },
                                    label = { Text("Balls/Shuttles") },
                                    prefix = { Text(currencySymbol) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("est_consumables_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Estimated Turnout Count",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Regular Members Adjuster
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Regular Members", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF1A1C1E))
                                    Text("Full season lists", fontSize = 10.sp, color = Color(0xFF5F6368))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (numMembers > 1) numMembers-- },
                                        modifier = Modifier.size(32.dp).background(Color(0xFFF1F3F4), CircleShape)
                                    ) {
                                        Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                                    }
                                    Text(
                                        text = "$numMembers",
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1A1C1E)
                                    )
                                    IconButton(
                                        onClick = { numMembers++ },
                                        modifier = Modifier.size(32.dp).background(Color(0xFFF1F3F4), CircleShape)
                                    ) {
                                        Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1C1E))
                                    }
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE1E2E5))
                            
                            // Guests Adjuster
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("One-Time Guests", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(0xFF1A1C1E))
                                    Text("Paying per game", fontSize = 10.sp, color = Color(0xFF5F6368))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (numGuests > 0) numGuests-- },
                                        modifier = Modifier.size(32.dp).background(Color(0xFFF1F3F4), CircleShape)
                                    ) {
                                        Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                                    }
                                    Text(
                                        text = "$numGuests",
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1A1C1E)
                                    )
                                    IconButton(
                                        onClick = { numGuests++ },
                                        modifier = Modifier.size(32.dp).background(Color(0xFFF1F3F4), CircleShape)
                                    ) {
                                        Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1C1E))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "P2P Guest Billing Rules",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Define guest tier contribution premium ratios.",
                                fontSize = 10.sp,
                                color = Color(0xFF5F6368)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val policies = listOf(
                                    "equal" to "Equal Shares",
                                    "fixed" to "Fixed Rate",
                                    "premium" to "1.5x Premium"
                                )
                                policies.forEach { (key, label) ->
                                    val isSelected = guestPolicy == key
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) sportTheme.primary.copy(alpha = 0.12f) else Color(0xFFF1F3F4))
                                            .border(1.dp, if (isSelected) sportTheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { guestPolicy = key }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) sportTheme.primary else Color(0xFF5F6368),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            
                            if (guestPolicy == "fixed") {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = fixedGuestFeeText,
                                    onValueChange = { fixedGuestFeeText = it },
                                    label = { Text("Flat Fee rate per Guest") },
                                    prefix = { Text(currencySymbol) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("fixed_guest_fee_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = sportTheme.primary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, sportTheme.primary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE QUOTED SHARE ESTIMATE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF5F6368)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "RECOVERY: 100%",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Member Rate", fontSize = 11.sp, color = Color(0xFF5F6368))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$currencySymbol${String.format("%.2f", memberCost)}",
                                        color = sportTheme.primary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Each ($numMembers playing)",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Guest Rate", fontSize = 11.sp, color = Color(0xFF5F6368))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$currencySymbol${String.format("%.2f", guestCost)}",
                                        color = Color(0xFFE65100),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Each ($numGuests playing)",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            val memberTotal = memberCost * numMembers
                            val guestTotal = guestCost * numGuests
                            val collectedTotal = (memberTotal + guestTotal).coerceAtLeast(1e-9)
                            val memberRatio = (memberTotal / collectedTotal).toFloat()
                            val guestRatio = (guestTotal / collectedTotal).toFloat()
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFE1E2E5))
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = "Estimated Target Budget Recovery Breakdowns:",
                                fontSize = 11.sp,
                                color = Color(0xFF5F6368)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE0E0E0))
                            ) {
                                if (memberRatio > 0.01) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(memberRatio.coerceAtLeast(0.01f))
                                            .background(sportTheme.primary)
                                    )
                                }
                                if (guestRatio > 0.01) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(guestRatio.coerceAtLeast(0.01f))
                                            .background(Color(0xFFE65100))
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(sportTheme.primary))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Members: $currencySymbol${String.format("%.2f", memberTotal)} (${(memberRatio * 100).toInt()}%)", fontSize = 10.sp, color = Color(0xFF1E1F20))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFE65100)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Guests: $currencySymbol${String.format("%.2f", guestTotal)} (${(guestRatio * 100).toInt()}%)", fontSize = 10.sp, color = Color(0xFF1E1F20))
                                }
                            }
                        }
                    }
                }

                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    Button(
                        onClick = {
                            val activeGroup = state.group
                            val quoteText = """
📢 *${activeGroup?.name ?: "Group"} Scheduled Turnout Announcement*
Sport Type: *${activeGroup?.sportType ?: "Other"}*
📅 Estimated split rate quotation:
---------------------------------------
• Anticipated Court costs: $currencySymbol${String.format(Locale.ROOT, "%.2f", courtVal)}
• Accessories / Shuttles estimate: $currencySymbol${String.format(Locale.ROOT, "%.2f", consVal)}
• Expected Roster Turnout: $numMembers Members, $numGuests Guests

💸 *ESTIMATED COST SPLIT ACCRUED*
👉 *Playing Members*: $currencySymbol${String.format(Locale.ROOT, "%.2f", memberCost)} each
👉 *Attending Guests*: $currencySymbol${String.format(Locale.ROOT, "%.2f", guestCost)} each

*(Estimations generated instantly via CourtLedger)*
Roster planning phase — Please confirm your slot with Admin ASAP! 🏸🔥
                            """.trimIndent()
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(quoteText))
                            showSafeToast(context, "Announce quote copied to Clipboard!", android.widget.Toast.LENGTH_SHORT)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("copy_forecast_quote"),
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy WhatsApp Proposal Quote", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) activeColor else Color(0xFFF0F1F3))
            .border(1.dp, if (selected) activeColor else Color(0xFFE1E2E5), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF1A1C1E),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PaymentsView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onLogPayment: () -> Unit,
    onLogBulkCost: () -> Unit,
    onDeletePayment: (Payment) -> Unit,
    onDeleteBulkCost: (BulkExpense) -> Unit,
    onEditBulkCost: (BulkExpense) -> Unit
) {
    val isAdmin = state.currentUser?.role == "Admin"
    var selectedSubTab by remember { mutableStateOf("settlements") } // "settlements", "bulk_costs"

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Ledger & Bulk Costs", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Settlements & group-wide bulk expenses", color = Color(0xFF5F6368), fontSize = 11.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onLogPayment,
                colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("record_payment_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Settle Payment", fontSize = 11.sp)
            }

            Button(
                onClick = onLogBulkCost,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F1F3), contentColor = Color(0xFF1A1C1E)),
                border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("log_bulk_cost_button")
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF1A1C1E), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Bulk Cost", fontSize = 11.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomFilterChip(
                selected = selectedSubTab == "settlements",
                onClick = { selectedSubTab = "settlements" },
                label = "Settlements (${state.payments.size})",
                activeColor = sportTheme.primary,
                modifier = Modifier.weight(1f)
            )

            CustomFilterChip(
                selected = selectedSubTab == "bulk_costs",
                onClick = { selectedSubTab = "bulk_costs" },
                label = "Bulk Costs (${state.bulkExpenses.size})",
                activeColor = sportTheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedSubTab == "settlements") {
            if (state.payments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No settlements logged yet.", color = Color(0xFF5F6368), fontSize = 14.sp)
                        Text("Log direct cash handoffs to settle active balances.", color = Color(0xFF5F6368), fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    items(state.payments) { pm ->
                        val currency = state.group?.currency ?: "$"
                        val payer = state.members.find { it.id == pm.memberId }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LocalAppColors.current.surface)
                                .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(sportTheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = sportTheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = payer?.name ?: "Unknown Player",
                                        color = LocalAppColors.current.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = pm.notes.ifBlank { "Hand-to-hand settlement" },
                                        color = LocalAppColors.current.textSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(pm.dateMillis)),
                                        color = LocalAppColors.current.textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currency${String.format("%.2f", pm.amount)}",
                                    color = Color(0xFF137333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isAdmin) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { onDeletePayment(pm) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Bulk Costs tracker sub tab
            if (state.bulkExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No bulk costs recorded yet.", color = Color(0xFF5F6368), fontSize = 14.sp)
                        Text("Easily record bulk buys like shuttles or balls that bypass guest players.", color = Color(0xFF5F6368), fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    items(state.bulkExpenses) { be ->
                        val currency = state.group?.currency ?: "$"
                        val buyer = state.members.find { it.id == be.paidByMemberId }
                        val regularMembers = state.members.filter { it.role == "Admin" || it.role == "Member" }
                        val regularCount = regularMembers.size.coerceAtLeast(1)
                        val individualShare = be.amount / regularCount

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LocalAppColors.current.surface)
                                .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(sportTheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = sportTheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = be.title,
                                        color = LocalAppColors.current.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Buyer: ${buyer?.name ?: "Unknown"}",
                                        color = LocalAppColors.current.textSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Split Share: $currency${String.format("%.2f", individualShare)} / regular user",
                                        color = sportTheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Distributed to $regularCount regular members. Guest/one-time users skipped.",
                                        color = LocalAppColors.current.textSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currency${String.format("%.2f", be.amount)}",
                                    color = LocalAppColors.current.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isAdmin) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { onEditBulkCost(be) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit bulk cost", tint = sportTheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteBulkCost(be) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete bulk cost", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// POP-UP CONFIGURATOR MODALS / OVERLAYS
// ----------------------------------------------------

@Composable
fun AddGroupOverlay(
    sportTheme: SportTheme,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("Badminton") }
    var selectedCurrency by remember { mutableStateOf("$") }
    var selectedTheme by remember { mutableStateOf("Teal") }

    val sports = listOf("Badminton", "Football", "Tennis", "Basketball", "Squash")
    val currencies = listOf("$", "£", "€", "¥", "₩")
    val themes = listOf("Teal", "Emerald", "Amber", "Indigo", "Crimson")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Create Sport Group", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_group_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Sport Type:", color = Color(0xFF1A1C1E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sports.forEach { s ->
                        FilterChip(
                            selected = selectedSport == s,
                            onClick = { selectedSport = s },
                            label = { Text(s) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sportTheme.primary,
                                selectedLabelColor = Color.White,
                                labelColor = Color(0xFF5F6368)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Currency:", color = Color(0xFF1A1C1E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { cur ->
                        FilterChip(
                            selected = selectedCurrency == cur,
                            onClick = { selectedCurrency = cur },
                            label = { Text(cur) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sportTheme.primary,
                                selectedLabelColor = Color.White,
                                labelColor = Color(0xFF5F6368)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Color Branding Set:", color = Color(0xFF1A1C1E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { t ->
                        FilterChip(
                            selected = selectedTheme == t,
                            onClick = { selectedTheme = t },
                            label = { Text(t) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sportTheme.primary,
                                selectedLabelColor = Color.White,
                                labelColor = Color(0xFF5F6368)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (groupName.isNotBlank()) onConfirm(groupName, selectedSport, selectedCurrency, selectedTheme) },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.testTag("add_group_submit_button")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMemberOverlay(
    sportTheme: SportTheme,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var memberEmail by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Member") }

    val roles = listOf("Member", "Admin", "One-time Player")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
            border = BorderStroke(1.dp, LocalAppColors.current.border),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Team Member", color = LocalAppColors.current.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalAppColors.current.textPrimary,
                        unfocusedTextColor = LocalAppColors.current.textPrimary,
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary,
                        unfocusedBorderColor = LocalAppColors.current.border,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_member_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = memberEmail,
                    onValueChange = { memberEmail = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalAppColors.current.textPrimary,
                        unfocusedTextColor = LocalAppColors.current.textPrimary,
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary,
                        unfocusedBorderColor = LocalAppColors.current.border,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_member_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Default Role Permissions:", color = LocalAppColors.current.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { r ->
                        FilterChip(
                            selected = selectedRole == r,
                            onClick = { selectedRole = r },
                            label = { Text(r, color = if (selectedRole == r) Color.White else LocalAppColors.current.textPrimary) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sportTheme.primary,
                                selectedLabelColor = Color.White,
                                labelColor = LocalAppColors.current.textSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (memberName.isNotBlank() && memberEmail.isNotBlank()) onConfirm(memberName, selectedRole, memberEmail) },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.testTag("add_member_submit_button")
                    ) {
                        Text("Add Member")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentOverlay(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    initialMemberId: Int? = null,
    initialAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String, Boolean) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf<Int?>(initialMemberId ?: state.members.firstOrNull()?.id) }
    var paymentAmount by remember { mutableStateOf(initialAmount?.let { String.format(Locale.ROOT, "%.2f", it) } ?: "") }
    var paymentNotes by remember { mutableStateOf("") }

    val selectedMember = state.members.find { it.id == selectedMemberId }
    val isGuest = selectedMember?.role == "One-time Player"

    var treatAsMinusBulk by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMemberId) {
        if (!isGuest) {
            treatAsMinusBulk = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
            border = BorderStroke(1.dp, LocalAppColors.current.border),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Log Payment / Settle Balance", color = LocalAppColors.current.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Payer:", color = LocalAppColors.current.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                var dropdownExpanded by remember { mutableStateOf(false) }
                val selectedName = selectedMember?.name ?: "Select Team Player"

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { dropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                            contentColor = LocalAppColors.current.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("payer_dropdown_trigger")
                    ) {
                        Text(selectedName, color = LocalAppColors.current.textPrimary)
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(LocalAppColors.current.surface)
                    ) {
                        state.members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name, color = LocalAppColors.current.textPrimary) },
                                onClick = {
                                    selectedMemberId = m.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { paymentAmount = it },
                    label = { Text(if (treatAsMinusBulk) "Deduction Amount (${state.group?.currency ?: "$"})" else "Amount Paid (${state.group?.currency ?: "$"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalAppColors.current.textPrimary,
                        unfocusedTextColor = LocalAppColors.current.textPrimary,
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary,
                        unfocusedLabelColor = LocalAppColors.current.textSecondary,
                        unfocusedBorderColor = LocalAppColors.current.border
                    ),
                    leadingIcon = if (treatAsMinusBulk) {
                        { Text(" - ", color = sportTheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                    } else null,
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paymentNotes,
                    onValueChange = { paymentNotes = it },
                    label = { Text("Payment Notes") },
                    placeholder = { Text("e.g. PayPal, Hand-to-hand, Venmo") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalAppColors.current.textPrimary,
                        unfocusedTextColor = LocalAppColors.current.textPrimary,
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary,
                        unfocusedLabelColor = LocalAppColors.current.textSecondary,
                        unfocusedBorderColor = LocalAppColors.current.border
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("payment_notes_input")
                )

                if (isGuest) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF1F3F4))
                            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(10.dp))
                            .clickable { treatAsMinusBulk = !treatAsMinusBulk }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = treatAsMinusBulk,
                            onCheckedChange = { treatAsMinusBulk = it },
                            colors = CheckboxDefaults.colors(checkedColor = sportTheme.primary),
                            modifier = Modifier.testTag("payment_treat_as_minus_checkbox")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Deduct from regular players' balances",
                                color = LocalAppColors.current.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Treats this payment as a negative bulk cost split equally among permanent regular members.",
                                color = LocalAppColors.current.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (treatAsMinusBulk && paymentAmount.toDoubleOrNull() != null) {
                    val amt = paymentAmount.toDoubleOrNull() ?: 0.0
                    val regularCount = state.members.filter { it.role == "Admin" || it.role == "Member" }.size.coerceAtLeast(1)
                    val shareReduction = amt / regularCount
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💡 Each of the $regularCount regular members will get a credit reduction of -${state.group?.currency ?: "$"}${String.format("%.2f", shareReduction)} on their balance.",
                        color = sportTheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = paymentAmount.toDoubleOrNull()
                            val memId = selectedMemberId
                            if (amt != null && memId != null) {
                                onConfirm(memId, amt, paymentNotes, treatAsMinusBulk)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.testTag("submit_payment_button")
                    ) {
                        Text(if (treatAsMinusBulk) "Log Deduction" else "Log Payment")
                    }
                }
            }
        }
    }
}

@Composable
fun SettlePlayerOverlay(
    memberBalance: MemberBalance,
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onDismiss: () -> Unit,
    onInstantSettle: (Double) -> Unit,
    onCustomSettle: () -> Unit,
    onDeletePayment: (Payment) -> Unit,
    onUpdateMember: (Member) -> Unit
) {
    val currency = state.group?.currency ?: "$"
    val bal = memberBalance.outstandingBalance

    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(memberBalance.member.name) { mutableStateOf(memberBalance.member.name) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() }
            .testTag("settle_player_overlay_background"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
            border = BorderStroke(1.dp, LocalAppColors.current.border),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = sportTheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Settle Balance with Player",
                    color = LocalAppColors.current.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (isEditingName) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            placeholder = { Text("Enter name") },
                            maxLines = 1,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LocalAppColors.current.textPrimary,
                                unfocusedTextColor = LocalAppColors.current.textPrimary,
                                focusedBorderColor = sportTheme.primary,
                                unfocusedBorderColor = LocalAppColors.current.border
                            ),
                            modifier = Modifier.weight(1f).height(50.dp).testTag("edit_member_name_input")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (editedName.isNotBlank()) {
                                    val updatedMember = memberBalance.member.copy(name = editedName.trim())
                                    onUpdateMember(updatedMember)
                                    isEditingName = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Name",
                                tint = Color(0xFF137333)
                            )
                        }
                        IconButton(
                            onClick = {
                                editedName = memberBalance.member.name
                                isEditingName = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Rename",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isEditingName = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = memberBalance.member.name,
                            color = LocalAppColors.current.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Player Name",
                            tint = sportTheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (bal > 0) {
                        sportTheme.primary.copy(alpha = 0.08f)
                    } else if (bal < 0) {
                        Color(0xFF81C784).copy(alpha = 0.1f)
                    } else {
                        LocalAppColors.current.border.copy(alpha = 0.2f)
                    },
                    border = BorderStroke(1.dp, if (bal > 0) sportTheme.primary.copy(alpha = 0.2f) else LocalAppColors.current.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (bal > 0) "Amount Owed to Admin" else if (bal < 0) "Amount Owed Back" else "Account Fully Settled",
                            fontSize = 12.sp,
                            color = LocalAppColors.current.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (bal == 0.0) "${currency}0.00" else "$currency${String.format(Locale.ROOT, "%.2f", Math.abs(bal))}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bal > 0) {
                                if (LocalAppColors.current.isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
                            } else if (bal < 0) {
                                if (LocalAppColors.current.isDark) Color(0xFF81C784) else Color(0xFF137333)
                            } else {
                                LocalAppColors.current.textPrimary
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (bal > 0.0) {
                    Button(
                        onClick = { onInstantSettle(bal) },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("instant_settle_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Settle Payment (${currency}${String.format(Locale.ROOT, "%.2f", bal)})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onCustomSettle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                        contentColor = LocalAppColors.current.textPrimary
                    ),
                    border = BorderStroke(1.dp, LocalAppColors.current.border),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("custom_settle_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = LocalAppColors.current.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (bal > 0.0) "Log Custom Payment Details" else "Log / Adjust Balance with custom amount", fontSize = 13.sp)
                }

                // List of existing settlements/payments for this member with option to delete
                val memberPayments = remember(state.payments, memberBalance.member.id) {
                    state.payments.filter { it.memberId == memberBalance.member.id }
                }

                if (memberPayments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = LocalAppColors.current.border.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recent Settlements / Payments",
                        color = LocalAppColors.current.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 130.dp)) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(memberPayments) { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LocalAppColors.current.border.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = p.notes.ifBlank { "Hand-to-hand settlement" },
                                            color = LocalAppColors.current.textPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(p.dateMillis)),
                                            color = LocalAppColors.current.textSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$currency${String.format(Locale.ROOT, "%.2f", p.amount)}",
                                            color = Color(0xFF137333),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { onDeletePayment(p) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete settlement",
                                                tint = Color(0xFFFF5252),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("settle_dialog_cancel_button")
                ) {
                    Text("Cancel", color = LocalAppColors.current.textSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AddBulkCostOverlay(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isPaidFromBank by remember { mutableStateOf(false) }
    // Only regular members can fund bulk expenses, or we default to the current user (if they are a regular member or admin)
    val regularMembers = state.members.filter { it.role == "Admin" || it.role == "Member" }
    var selectedMemberId by remember { mutableStateOf<Int?>(regularMembers.find { it.isCurrentUser }?.id ?: regularMembers.firstOrNull()?.id) }
    val selectedName = regularMembers.find { it.id == selectedMemberId }?.name ?: "Select Purchaser"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Log Bulk Group Expense", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "This cost splits exclusively with permanent Regular Members (Admin/Member roles). Guest/One-time players are skipped.",
                    color = Color(0xFF5F6368),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item / Expense Title") },
                    placeholder = { Text("e.g., Bulk buying shuttles, Football net") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("bulk_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Total Cost Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("bulk_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!isPaidFromBank) {
                    Text("Who purchased/funded this?", color = Color(0xFF1A1C1E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    var dropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { dropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                                contentColor = LocalAppColors.current.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("bulk_buyer_dropdown")
                        ) {
                            Text(selectedName, color = LocalAppColors.current.textPrimary)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(LocalAppColors.current.surface)
                        ) {
                            regularMembers.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name, color = LocalAppColors.current.textPrimary) },
                                    onClick = {
                                        selectedMemberId = m.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPaidFromBank,
                        onCheckedChange = { isPaidFromBank = it },
                        colors = CheckboxDefaults.colors(checkedColor = sportTheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pay from Group Bank Account (Balance: ${state.group?.currency ?: "$"}${String.format("%.2f", state.bankBalance)})",
                        color = Color(0xFF1A1C1E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            // If isPaidFromBank is true, we can assign the buyer as the first available member (it is not relevant since splits are 0 and no payer is credited)
                            val memId = if (isPaidFromBank) (regularMembers.firstOrNull()?.id ?: 0) else selectedMemberId
                            if (title.isNotBlank() && amt != null && amt > 0 && memId != null) {
                                onConfirm(title, amt, memId, isPaidFromBank)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.testTag("submit_bulk_cost_button")
                    ) {
                        Text("Log Bulk Expense")
                    }
                }
            }
        }
    }
}

// Dialog to log fully customized sessions with automatic splitting & overrides
@Composable
fun AddSessionOverlay(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onDismiss: () -> Unit,
    onConfirm: (notes: String, dateMillis: Long, expenses: List<ExpenseItem>, attendanceList: List<Attendance>, isPaidFromBank: Boolean) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var isPaidFromBank by remember { mutableStateOf(false) }

    // Dynamic Expense entries
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseList by remember { mutableStateOf(mutableListOf<ExpenseItem>()) }

    // Participant Attendance checks & custom overrides
    val attendanceStates = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            state.members.forEach { m -> put(m.id, true) } // default attendees present
        }
    }
    val overrideStates = remember {
        mutableStateMapOf<Int, String>().apply {
            state.members.forEach { m -> put(m.id, "") }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Text("Log Play Session & Play Details", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Session Notes / Description") },
                        placeholder = { Text("e.g. Competitive evening singles/doubles") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1C1E),
                            unfocusedTextColor = Color(0xFF1A1C1E),
                            focusedLabelColor = sportTheme.primary,
                            focusedBorderColor = sportTheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("session_notes_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Add Expense Item (Multiple Allowed):", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = { expenseName = it },
                            label = { Text("Label") },
                            placeholder = { Text("Court rental") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1A1C1E),
                                unfocusedTextColor = Color(0xFF1A1C1E),
                                focusedLabelColor = sportTheme.primary,
                                focusedBorderColor = sportTheme.primary
                            ),
                            modifier = Modifier.weight(1.5f).height(56.dp).testTag("expense_name_input")
                        )

                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Price") },
                            placeholder = { Text("20.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1A1C1E),
                                unfocusedTextColor = Color(0xFF1A1C1E),
                                focusedLabelColor = sportTheme.primary,
                                focusedBorderColor = sportTheme.primary
                            ),
                            modifier = Modifier.weight(1f).height(56.dp).testTag("expense_amount_input")
                        )

                        Button(
                            onClick = {
                                val amt = expenseAmount.toDoubleOrNull()
                                if (expenseName.isNotBlank() && amt != null) {
                                    val newList = ArrayList(expenseList)
                                    newList.add(ExpenseItem(sessionId = 0, name = expenseName, amount = amt))
                                    expenseList = newList
                                    expenseName = ""
                                    expenseAmount = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.CenterVertically).testTag("add_expense_pill")
                        ) {
                            Text("Add")
                        }
                    }

                    // Displayed expenses live stream
                    if (expenseList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        expenseList.forEach { exp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF0F1F3))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(exp.name, color = Color(0xFF1A1C1E), fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${state.group?.currency ?: "$"}${String.format("%.2f", exp.amount)}",
                                        color = sportTheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color(0xFF5F6368),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val copy = ArrayList(expenseList)
                                                copy.remove(exp)
                                                expenseList = copy
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Attendance Roster, Custom Split Overrides:", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Uncheck absent players. Enter override prices to exclude them from the split calculator.", color = Color(0xFF5F6368), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    state.members.forEach { member ->
                        val isAttended = attendanceStates[member.id] ?: false
                        val currentOverride = overrideStates[member.id] ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.5f)
                             ) {
                                Checkbox(
                                    checked = isAttended,
                                    onCheckedChange = { attendanceStates[member.id] = it },
                                    colors = CheckboxDefaults.colors(checkedColor = sportTheme.primary)
                                )
                                Text(member.name, color = if (isAttended) Color(0xFF1A1C1E) else Color(0xFF5F6368), fontSize = 13.sp)
                            }

                            if (isAttended) {
                                OutlinedTextField(
                                    value = currentOverride,
                                    onValueChange = { overrideStates[member.id] = it },
                                    placeholder = { Text("Override amount", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF1A1C1E),
                                        unfocusedTextColor = Color(0xFF1A1C1E),
                                        focusedBorderColor = sportTheme.primary,
                                        unfocusedBorderColor = Color(0xFFE1E2E5)
                                    ),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(44.dp)
                                )
                            } else {
                                Box(modifier = Modifier.width(130.dp)) // empty spaceholder
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = isPaidFromBank,
                        onCheckedChange = { isPaidFromBank = it },
                        colors = CheckboxDefaults.colors(checkedColor = sportTheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pay from Group Bank Account (Balance: ${state.group?.currency ?: "$"}${String.format("%.2f", state.bankBalance)})",
                        color = Color(0xFF1A1C1E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = Color(0xFFE1E2E5), modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cumulativeSum = expenseList.sumOf { it.amount }
                    Text(
                        text = "Sum cost: ${state.group?.currency ?: "$"}${String.format("%.2f", cumulativeSum)}",
                        color = Color(0xFF1A1C1E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (expenseList.isNotEmpty()) {
                                    val finalAttendanceList = state.members.map { m ->
                                        val present = attendanceStates[m.id] ?: false
                                        val overrideVal = if (present) overrideStates[m.id]?.toDoubleOrNull() else null
                                        Attendance(
                                            sessionId = 0,
                                            memberId = m.id,
                                            isPresent = present,
                                            costOverride = overrideVal
                                        )
                                    }
                                    onConfirm(notes, System.currentTimeMillis(), expenseList, finalAttendanceList, isPaidFromBank)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                            modifier = Modifier.testTag("save_session_submit_button")
                        ) {
                            Text("Save Logs")
                        }
                    }
                }
            }
        }
    }
}

// Simulated automated email dashboard output overlay view
@Composable
fun EmailLogsOverlay(
    logs: List<String>,
    sportTheme: SportTheme,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Weekly Email Summaries", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear logs", tint = Color(0xFFFF5252))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No emails sent. Trigger from the dashboard!", color = Color(0xFF5F6368), fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(logs) { log ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8F9FA))
                                    .border(1.dp, Color(0xFFE1E2E5), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = log,
                                    color = Color(0xFF3C4043),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close Panel")
                }
            }
        }
    }
}

@Composable
fun EditBulkCostOverlay(
    bulkExpense: BulkExpense,
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onDismiss: () -> Unit,
    onConfirm: (BulkExpense) -> Unit
) {
    var title by remember { mutableStateOf(bulkExpense.title) }
    var amount by remember { mutableStateOf(bulkExpense.amount.toString()) }
    val regularMembers = state.members.filter { it.role == "Admin" || it.role == "Member" }
    var selectedMemberId by remember { mutableStateOf<Int?>(bulkExpense.paidByMemberId) }
    val selectedName = regularMembers.find { it.id == selectedMemberId }?.name ?: "Select Purchaser"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit Bulk Group Expense", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Modify the bulk cost title, amount of funding, or the regular member who purchased it.",
                    color = Color(0xFF5F6368),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item / Expense Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_bulk_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Total Cost Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_bulk_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Who purchased/funded this?", color = LocalAppColors.current.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                var dropdownExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { dropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                            contentColor = LocalAppColors.current.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_bulk_buyer_dropdown")
                    ) {
                        Text(selectedName, color = LocalAppColors.current.textPrimary)
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(LocalAppColors.current.surface)
                    ) {
                        regularMembers.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name, color = LocalAppColors.current.textPrimary) },
                                onClick = {
                                    selectedMemberId = m.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            val memId = selectedMemberId
                            if (title.isNotBlank() && amt != null && amt > 0 && memId != null) {
                                onConfirm(
                                    bulkExpense.copy(
                                        title = title,
                                        amount = amt,
                                        paidByMemberId = memId
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.testTag("submit_edit_bulk_cost_button")
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    themeMode: String,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateGroup: (String, String, String, String) -> Unit,
    viewModel: CourtLedgerViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val isAdmin = state.currentUser?.role == "Admin"

    var showImportDialog by remember { mutableStateOf(false) }
    var pastedBackupText by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var groupName by remember(state.group) { mutableStateOf(state.group?.name ?: "") }
    var selectedSport by remember(state.group) { mutableStateOf(state.group?.sportType ?: "Badminton") }
    var selectedCurrency by remember(state.group) { mutableStateOf(state.group?.currency ?: "$") }
    var selectedThemeColor by remember(state.group) { mutableStateOf(state.group?.themeColor ?: "Teal") }

    val sports = listOf("Badminton", "Football", "Tennis", "Squash")
    val currencies = listOf("$", "£", "€", "¥", "Rp", "RM", "Rs")
    val themes = listOf("Teal", "Emerald", "Amber", "Indigo", "Crimson")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Theme Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                border = BorderStroke(1.dp, LocalAppColors.current.border),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = sportTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App Theme Mode",
                            color = LocalAppColors.current.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Choose between Light, Dark, or System Default appearance.",
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            "system" to "System",
                            "light" to "Light",
                            "dark" to "Dark"
                        )
                        modes.forEach { (modeKey, modeLabel) ->
                            val isSelected = themeMode == modeKey
                            val icon = when (modeKey) {
                                "light" -> Icons.Default.Star
                                "dark" -> Icons.Default.Lock
                                else -> Icons.Default.Settings
                            }
                            Button(
                                onClick = { onUpdateThemeMode(modeKey) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) sportTheme.primary else (if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3)),
                                    contentColor = if (isSelected) Color.White else LocalAppColors.current.textPrimary
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, LocalAppColors.current.border),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("theme_btn_$modeKey")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isSelected) Color.White else LocalAppColors.current.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = modeLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Group Configurations Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                border = BorderStroke(1.dp, LocalAppColors.current.border),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = sportTheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Group & Theme Settings",
                            color = LocalAppColors.current.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Customize active sports ledger profile and local theme accents.",
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (!isAdmin) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (LocalAppColors.current.isDark) Color(0xFF664D03) else Color(0xFFFFF3CD))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "🔒 Group configuration details are locked. Only local Group Admins can edit group parameters.",
                                color = if (LocalAppColors.current.isDark) Color(0xFFFFF3CD) else Color(0xFF856404),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Group Name Input
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { if (isAdmin) groupName = it },
                        readOnly = !isAdmin,
                        label = { Text("Group Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalAppColors.current.textPrimary,
                            unfocusedTextColor = LocalAppColors.current.textPrimary,
                            focusedLabelColor = sportTheme.primary,
                            focusedBorderColor = sportTheme.primary,
                            unfocusedBorderColor = LocalAppColors.current.border
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_group_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sport Type Selection
                    Text("Sport Type", color = LocalAppColors.current.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    var sportExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { if (isAdmin) sportExpanded = true },
                            enabled = isAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                                contentColor = LocalAppColors.current.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("settings_sport_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedSport, color = LocalAppColors.current.textPrimary, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LocalAppColors.current.textPrimary)
                            }
                        }
                        DropdownMenu(
                            expanded = sportExpanded,
                            onDismissRequest = { sportExpanded = false },
                            modifier = Modifier.background(LocalAppColors.current.surface)
                        ) {
                            sports.forEach { sp ->
                                DropdownMenuItem(
                                    text = { Text(sp, color = LocalAppColors.current.textPrimary) },
                                    onClick = {
                                        selectedSport = sp
                                        sportExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Currency Symbol
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Currency Symbol", color = LocalAppColors.current.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            var currencyExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { if (isAdmin) currencyExpanded = true },
                                    enabled = isAdmin,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                                        contentColor = LocalAppColors.current.textPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("settings_currency_dropdown")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedCurrency, color = LocalAppColors.current.textPrimary, fontSize = 13.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LocalAppColors.current.textPrimary)
                                    }
                                }
                                DropdownMenu(
                                    expanded = currencyExpanded,
                                    onDismissRequest = { currencyExpanded = false },
                                    modifier = Modifier.background(LocalAppColors.current.surface)
                                ) {
                                    currencies.forEach { cur ->
                                        DropdownMenuItem(
                                            text = { Text(cur, color = LocalAppColors.current.textPrimary) },
                                            onClick = {
                                                selectedCurrency = cur
                                                currencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Theme Accents", color = LocalAppColors.current.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            var themeExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { if (isAdmin) themeExpanded = true },
                                    enabled = isAdmin,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                                        contentColor = LocalAppColors.current.textPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("settings_theme_dropdown")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedThemeColor, color = LocalAppColors.current.textPrimary, fontSize = 13.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LocalAppColors.current.textPrimary)
                                    }
                                }
                                DropdownMenu(
                                    expanded = themeExpanded,
                                    onDismissRequest = { themeExpanded = false },
                                    modifier = Modifier.background(LocalAppColors.current.surface)
                                ) {
                                    themes.forEach { th ->
                                        DropdownMenuItem(
                                            text = { Text(th, color = LocalAppColors.current.textPrimary) },
                                            onClick = {
                                                selectedThemeColor = th
                                                themeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    onUpdateGroup(groupName, selectedSport, selectedCurrency, selectedThemeColor)
                                    showSafeToast(context, "Group profile successfully updated!", android.widget.Toast.LENGTH_SHORT)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("save_settings_button")
                        ) {
                            Text("Save Group Profile Changes")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                showDeleteConfirmDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            ),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("delete_group_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete This Group", color = Color.Red)
                        }

                        if (showDeleteConfirmDialog) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showDeleteConfirmDialog = false },
                                title = { Text("Delete Group?", color = LocalAppColors.current.textPrimary) },
                                text = { 
                                    Text(
                                        "Are you absolutely sure you want to delete \"${state.group?.name}\"?\n\nThis will permanently erase all associated play sessions, players, expenses, and payment records. This action cannot be undone.",
                                        color = LocalAppColors.current.textSecondary
                                    ) 
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteConfirmDialog = false
                                            state.group?.id?.let { gid ->
                                                coroutineScope.launch {
                                                    val grps = viewModel.allGroups.value
                                                    if (grps.size <= 1) {
                                                        showSafeToast(context, "Cannot delete the last remaining group! Create another group first.", android.widget.Toast.LENGTH_LONG)
                                                    } else {
                                                        val done = viewModel.deleteGroupCascaded(gid)
                                                        if (done) {
                                                            showSafeToast(context, "Group successfully deleted.", android.widget.Toast.LENGTH_SHORT)
                                                        } else {
                                                            showSafeToast(context, "Failed to delete group.", android.widget.Toast.LENGTH_SHORT)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                        Text("Cancel", color = LocalAppColors.current.textPrimary)
                                    }
                                },
                                containerColor = LocalAppColors.current.surface,
                                titleContentColor = LocalAppColors.current.textPrimary,
                                textContentColor = LocalAppColors.current.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // 2. Data Backup & CSV Export Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                border = BorderStroke(1.dp, LocalAppColors.current.border),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = sportTheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup, Export & Device Transfer",
                            color = LocalAppColors.current.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Export your complete attendance, session logs, bulk expenses, and payment history into a universally compatible CSV format. Reupload this backup file on any device to seamlessly migrate or restore your entire app state.",
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // EXPORT LABEL
                    Text(
                        text = "EXPORT LEDGER DATABASE",
                        color = sportTheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = viewModel.exportAllToCsvString()
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(csv))
                                showSafeToast(context, "Full Backup CSV copied to clipboard!", android.widget.Toast.LENGTH_SHORT)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("copy_csv_backup_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Backup CSV Code", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // IMPORT LABEL
                    Text(
                        text = "IMPORT SYSTEM RESTORE",
                        color = sportTheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            showImportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                            contentColor = LocalAppColors.current.textPrimary
                        ),
                        border = BorderStroke(1.dp, LocalAppColors.current.border),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("paste_csv_import_button")
                    ) {
                        Text("Paste CSV Text to Restore", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Nice metadata display of the entries included in the CSV file
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF8F9FA))
                            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Current Ledger Content Metrics", color = LocalAppColors.current.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• Registered Members: ${state.members.size}", color = LocalAppColors.current.textSecondary, fontSize = 10.sp)
                            Text("• Play Sessions: ${state.sessions.size}", color = LocalAppColors.current.textSecondary, fontSize = 10.sp)
                            Text("• Direct Settlement Payments: ${state.payments.size}", color = LocalAppColors.current.textSecondary, fontSize = 10.sp)
                            Text("• Group Bulk Costs: ${state.bulkExpenses.size}", color = LocalAppColors.current.textSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showImportDialog = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface,
                border = BorderStroke(1.dp, LocalAppColors.current.border),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Paste Backup CSV Code",
                        color = LocalAppColors.current.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste the entire exported backup CSV text code below. This will wipe the existing database and restore all matched data correctly.",
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pastedBackupText,
                        onValueChange = { pastedBackupText = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp).testTag("backup_text_field"),
                        placeholder = { Text("Paste CSV backup code here...", fontSize = 12.sp, color = LocalAppColors.current.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalAppColors.current.textPrimary,
                            unfocusedTextColor = LocalAppColors.current.textPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = sportTheme.primary,
                            unfocusedBorderColor = LocalAppColors.current.border
                        ),
                        textStyle = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showImportDialog = false
                                pastedBackupText = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (LocalAppColors.current.isDark) Color(0xFF2E3134) else Color(0xFFF0F1F3),
                                contentColor = LocalAppColors.current.textPrimary
                            ),
                            border = BorderStroke(1.dp, LocalAppColors.current.border),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (pastedBackupText.isNotBlank()) {
                                    coroutineScope.launch {
                                        val success = viewModel.importAllFromCsvString(pastedBackupText)
                                        if (success) {
                                            showSafeToast(context, "Full Backup restored successfully!", android.widget.Toast.LENGTH_LONG)
                                            showImportDialog = false
                                            pastedBackupText = ""
                                        } else {
                                            showSafeToast(context, "Import failed: Invalid CSV code sequence.", android.widget.Toast.LENGTH_LONG)
                                        }
                                    }
                                } else {
                                    showSafeToast(context, "Please paste some CSV content.", android.widget.Toast.LENGTH_SHORT)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text("Restore", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

fun generateGroupCsv(state: GroupDashboardState): String {
    val currency = state.group?.currency ?: "$"
    return buildString {
        appendLine("--- COURTLEDGER DATA BACKUP ---")
        appendLine("Group: ${state.group?.name ?: "Unknown"}")
        appendLine("Sport: ${state.group?.sportType ?: "Other"}")
        appendLine("Currency: $currency")
        appendLine("Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        appendLine()

        // 1. Members
        appendLine("=== MEMBERS ===")
        appendLine("Member ID,Name,Email,Role,Total Cost Share,Total Paid,Outstanding Balance")
        state.balances.forEach { b ->
            appendLine("${b.member.id},\"${b.member.name}\",\"${b.member.email}\",\"${b.member.role}\",${String.format(java.util.Locale.ROOT, "%.2f", b.totalCostShare)},${String.format(java.util.Locale.ROOT, "%.2f", b.totalPaid)},${String.format(java.util.Locale.ROOT, "%.2f", b.outstandingBalance)}")
        }
        appendLine()

        // 2. Play Sessions
        appendLine("=== SESSIONS AND SPLITS ===")
        appendLine("Session ID,Date,Notes,Total Cost,Expense Items,Attendees")
        state.sessionDetails.forEach { sd ->
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(sd.session.dateMillis))
            val expenseStr = sd.expenseItems.joinToString(";") { "${it.name}:${it.amount}" }
            val attendeeStr = sd.attendees.joinToString(";") { it.name }
            appendLine("${sd.session.id},$dateStr,\"${sd.session.notes}\",${String.format(java.util.Locale.ROOT, "%.2f", sd.session.totalCost)},\"$expenseStr\",\"$attendeeStr\"")
        }
        appendLine()

        // 3. Direct Payments / Settlements
        appendLine("=== SETTLEMENT LOGS ===")
        appendLine("Payment ID,Date,Member Name,Amount,Notes")
        state.payments.forEach { pm ->
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(pm.dateMillis))
            val payerName = state.members.find { it.id == pm.memberId }?.name ?: "Unknown"
            appendLine("${pm.id},$dateStr,\"$payerName\",${String.format(java.util.Locale.ROOT, "%.2f", pm.amount)},\"${pm.notes}\"")
        }
        appendLine()

        // 4. Bulk Expenses
        appendLine("=== BULK GROUP EXPENSES ===")
        appendLine("Bulk Cost ID,Date,Title,Paid By,Individual Regular Share,Total Amount,Notes")
        state.bulkExpenses.forEach { be ->
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(be.dateMillis))
            val paidByName = state.members.find { it.id == be.paidByMemberId }?.name ?: "Unknown"
            val regularMembers = state.members.filter { it.role == "Admin" || it.role == "Member" }
            val regularCount = regularMembers.size.coerceAtLeast(1)
            val share = be.amount / regularCount
            appendLine("${be.id},$dateStr,\"${be.title}\",\"$paidByName\",${String.format(java.util.Locale.ROOT, "%.2f", share)},${String.format(java.util.Locale.ROOT, "%.2f", be.amount)},\"${be.notes}\"")
        }
    }
}

// ----------------------------------------------------
// GROUP BANK ACCOUNT TAB VIEW
// ----------------------------------------------------

@Composable
fun GroupBankAccountView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onAddTransaction: (description: String, amount: Double) -> Unit
) {
    // Deprecated. Successfully integrated into the dashboard's "Treasury" subtab structure.
}

/*
@Composable
fun DeprecatedGroupBankAccountView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onAddTransaction: (description: String, amount: Double) -> Unit
) {
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Feature Header and Explainer
        Text(
            text = "Group Bank Account",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E)
        )
        Text(
            text = "An pooled treasury for excess player payments and general court resources. Group expenses can be funded directly from this balance, and individual payment overages flow here automatically.",
            color = Color(0xFF5F6368),
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Balance Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = sportTheme.primary.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, sportTheme.primary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL TREASURY BALANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = sportTheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currency${String.format("%.2f", state.bankBalance)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1C1E)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons for Bank Contributions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            transactionType = "deposit"
                            showManualDepositDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sportTheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("bank_deposit_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Manual Deposit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            transactionType = "withdrawal"
                            showManualDepositDialog = true
                        },
                        border = BorderStroke(1.dp, Color(0xFFCDD0D5)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1A1C1E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("bank_withdrawal_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Manual Extraction",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Deduction", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions Header
        Text(
            text = "Transaction History",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E)
        )
        Text(
            text = "Ledger list of all additions and subtractions from the group treasury",
            color = Color(0xFF5F6368),
            fontSize = 11.sp,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.bankTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF7F8FA), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Empty Board",
                        tint = Color(0xFF8C9199),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No bank activity yet",
                        color = Color(0xFF5F6368),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.bankTransactions.sortedByDescending { it.dateMillis }) { tx ->
                    val isAddition = tx.amount >= 0.0
                    val absVal = Math.abs(tx.amount)
                    val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(tx.dateMillis))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7F8FA), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE1E2E5), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isAddition) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAddition) Icons.Default.CheckCircle else Icons.Default.Delete,
                                    contentDescription = "Transaction Logo",
                                    tint = if (isAddition) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.description,
                                        color = Color(0xFF1A1C1E),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (tx.isSystemOverpayment) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(sportTheme.primary.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Overfill", fontSize = 8.sp, color = sportTheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateStr,
                                    color = Color(0xFF5F6368),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "${if (isAddition) "+" else "-"} $currency${String.format("%.2f", absVal)}",
                            color = if (isAddition) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    if (showManualDepositDialog) {
        var txDesc by remember { mutableStateOf("") }
        var txAmt by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable { showManualDepositDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {},
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (transactionType == "deposit") "Record Manual Contribution" else "Record Manual Deduction",
                        color = Color(0xFF1A1C1E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Manually record a cash movement on behalf of the group ledger.",
                        color = Color(0xFF5F6368),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = txDesc,
                        onValueChange = { txDesc = it },
                        label = { Text("Reason / Description") },
                        placeholder = { Text(if (transactionType == "deposit") "e.g. Bank interest refund, cash donation" else "e.g., Equipment repair cash out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1C1E),
                            unfocusedTextColor = Color(0xFF1A1C1E),
                            focusedLabelColor = sportTheme.primary,
                            focusedBorderColor = sportTheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_tx_desc")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = txAmt,
                        onValueChange = { txAmt = it },
                        label = { Text("Cost Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1C1E),
                            unfocusedTextColor = Color(0xFF1A1C1E),
                            focusedLabelColor = sportTheme.primary,
                            focusedBorderColor = sportTheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_tx_amount")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showManualDepositDialog = false }) { Text("Cancel", color = Color.Gray) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val amtDouble = txAmt.toDoubleOrNull()
                                if (txDesc.isNotBlank() && amtDouble != null && amtDouble > 0) {
                                    val finalAmt = if (transactionType == "deposit") amtDouble else -amtDouble
                                    onAddTransaction(txDesc, finalAmt)
                                    showManualDepositDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = sportTheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("bank_tx_submit")
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}
*/
