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
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: CourtLedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CourtLedgerApp(viewModel)
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

    var activeTab by remember { mutableStateOf("dashboard") } // "dashboard", "sessions", "payments"

    // Dialog trigger states
    var showAddGroup by remember { mutableStateOf(false) }
    var showAddMember by remember { mutableStateOf(false) }
    var showAddSession by remember { mutableStateOf(false) }
    var showAddPayment by remember { mutableStateOf(false) }
    var showEmailLogs by remember { mutableStateOf(false) }

    // Selected sport theme
    val sportTheme = getSportTheme(
        sportName = state.group?.sportType ?: "",
        themeSetting = state.group?.themeColor ?: "Teal"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F9FB), // Clean Minimalism background
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = Color(0xFFE1E2E5),
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
                        .background(Color.White)
                        .drawBehind {
                            drawLine(
                                color = Color(0xFFE1E2E5),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(sportTheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (state.group?.name?.take(1) ?: "C"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CourtLedger",
                                    color = Color(0xFF131416),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = state.group?.sportType ?: "Cost Splitting Screen",
                                    color = Color(0xFF5F6368),
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
                                    containerColor = Color(0xFFF0F1F3),
                                    contentColor = Color(0xFF1A1C1E)
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
                                modifier = Modifier.background(Color.White)
                            ) {
                                groups.forEach { grp ->
                                    DropdownMenuItem(
                                        text = { Text(grp.name, color = Color(0xFF1A1C1E)) },
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
                                HorizontalDivider(color = Color(0xFFE1E2E5))
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active User Profile simulator "Admin vs Member roles"
                    if (state.members.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F1F3))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (state.currentUser?.role == "Admin") sportTheme.primary else Color.Gray
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (state.currentUser?.role == "Admin") Icons.Default.Settings else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Role: ${state.currentUser?.name ?: "Guest"} (${state.currentUser?.role ?: "Member"})",
                                        color = Color(0xFF1A1C1E),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Simulate view as another member:",
                                        color = Color(0xFF5F6368),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            var userMenuExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { userMenuExpanded = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1C1E)),
                                    border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
                                    modifier = Modifier.height(28.dp).testTag("roles_simulation_button")
                                ) {
                                    Text("Switch", fontSize = 10.sp, color = Color(0xFF1A1C1E))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1A1C1E), modifier = Modifier.size(12.dp))
                                }
                                DropdownMenu(
                                    expanded = userMenuExpanded,
                                    onDismissRequest = { userMenuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    state.members.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text("${m.name} [${m.role}]", color = Color(0xFF1A1C1E), fontSize = 13.sp) },
                                            onClick = {
                                                viewModel.switchUser(m.id)
                                                userMenuExpanded = false
                                            }
                                        )
                                    }
                                }
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
                            onWeeklyEmail = {
                                viewModel.simulateWeeklyEmailSummary()
                                showEmailLogs = true
                            },
                            onEmailSimHistory = { showEmailLogs = true },
                            emailLogCount = emailLogs.size
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
                            onDeletePayment = { viewModel.deletePayment(it) }
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
                    onDismiss = { showAddPayment = false },
                    onConfirm = { memberId, amount, notes ->
                        viewModel.addPayment(memberId, amount, notes)
                        showAddPayment = false
                    }
                )
            }

            if (showAddSession) {
                AddSessionOverlay(
                    state = state,
                    sportTheme = sportTheme,
                    onDismiss = { showAddSession = false },
                    onConfirm = { notes, dateMillis, expenses, attendanceList ->
                        viewModel.addSession(notes, dateMillis, expenses, attendanceList)
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

@Composable
fun DashboardView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onAddMember: () -> Unit,
    onLogPayment: () -> Unit,
    onWeeklyEmail: () -> Unit,
    onEmailSimHistory: () -> Unit,
    emailLogCount: Int
) {
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1E2E5), contentColor = Color(0xFF1A1C1E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("quick_add_member_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1A1C1E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Member", fontSize = 12.sp, color = Color(0xFF1A1C1E))
                        }
                    }
                }
            }
        }

        // Email simulation triggers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE1E2E5), RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Automated Email Splitting Summaries", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Trigger simulated RF.GD email outputs summarizing weekly balances for sport players.", color = Color(0xFF5F6368), fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Button(
                        onClick = onWeeklyEmail,
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp).testTag("send_email_summary_btn")
                    ) {
                        Text("Send Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (emailLogCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "History ($emailLogCount)",
                            color = sportTheme.primary,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { onEmailSimHistory() }
                                .padding(2.dp)
                        )
                    }
                }
            }
        }

        // Member balances roster header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Player Ledger Balances",
                    color = Color(0xFF1A1C1E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${state.members.size} members",
                    color = Color(0xFF5F6368),
                    fontSize = 12.sp
                )
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
                    Text("No players. Use buttons to customize members!", color = Color(0xFF5F6368), fontSize = 14.sp)
                }
            }
        } else {
            items(state.balances) { b ->
                val currency = state.group?.currency ?: "$"
                val isMe = b.member.id == state.currentUser?.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(
                            1.dp,
                            if (isMe) sportTheme.primary.copy(alpha = 0.5f) else Color(0xFFE1E2E5),
                            RoundedCornerShape(12.dp)
                        )
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
                                    if (b.member.role == "Admin") sportTheme.primary.copy(alpha = 0.15f) else Color(0xFFF0F1F3)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = b.member.name.take(2).uppercase(Locale.ROOT),
                                color = if (b.member.role == "Admin") sportTheme.primary else Color(0xFF5F6368),
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
                                    color = Color(0xFF1A1C1E)
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
                            }
                            Text(
                                text = "${b.member.role} • ${b.member.email}",
                                color = Color(0xFF5F6368),
                                fontSize = 11.sp
                              )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val bal = b.outstandingBalance
                        Text(
                            text = if (bal > 0) "owes admin" else if (bal < 0) "owed back" else "all settled",
                            color = Color(0xFF5F6368),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (bal == 0.0) "${currency}0.00" else "$currency${String.format("%.2f", Math.abs(bal))}",
                            color = if (bal > 0) Color(0xFFB3261E) else if (bal < 0) Color(0xFF137333) else Color(0xFF1A1C1E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
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

            if (isAdmin) {
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE1E2E5)),
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
                                        color = Color(0xFF1A1C1E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = entry.session.notes.ifBlank { "Generic Sports Play" },
                                        color = Color(0xFF5F6368),
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
    }
}

@Composable
fun PaymentsView(
    state: GroupDashboardState,
    sportTheme: SportTheme,
    onLogPayment: () -> Unit,
    onDeletePayment: (Payment) -> Unit
) {
    val isAdmin = state.currentUser?.role == "Admin"

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Payment Ledger Logs", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Settled debts & contributions", color = Color(0xFF5F6368), fontSize = 12.sp)
            }

            Button(
                onClick = onLogPayment,
                colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("record_payment_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Payment", fontSize = 12.sp)
            }
        }

        if (state.payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No payments recorded yet.", color = Color(0xFF5F6368), fontSize = 14.sp)
                    Text("Pay or settle with the administrator to clean balances!", color = Color(0xFF5F6368), fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
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
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE1E2E5), RoundedCornerShape(12.dp))
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
                                    color = Color(0xFF1A1C1E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = pm.notes.ifBlank { "Hand-to-hand settlement" },
                                    color = Color(0xFF5F6368),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(pm.dateMillis)),
                                    color = Color(0xFF5F6368),
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
                                IconButton(onClick = { onDeletePayment(pm) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
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

    val roles = listOf("Member", "Admin")

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
                Text("Add Team Member", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
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
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_member_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Default Role Permissions:", color = Color(0xFF5F6368), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { r ->
                        FilterChip(
                            selected = selectedRole == r,
                            onClick = { selectedRole = r },
                            label = { Text(r) },
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
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf<Int?>(state.members.firstOrNull()?.id) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentNotes by remember { mutableStateOf("") }

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
                Text("Log Payment / Settle Balance", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Payer:", color = Color(0xFF1A1C1E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                var dropdownExpanded by remember { mutableStateOf(false) }
                val selectedName = state.members.find { it.id == selectedMemberId }?.name ?: "Select Team Player"

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { dropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F1F3), contentColor = Color(0xFF1A1C1E)),
                        modifier = Modifier.fillMaxWidth().testTag("payer_dropdown_trigger")
                    ) {
                        Text(selectedName, color = Color(0xFF1A1C1E))
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        state.members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name, color = Color(0xFF1A1C1E)) },
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
                    label = { Text("Amount Paid (${state.group?.currency ?: "$"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paymentNotes,
                    onValueChange = { paymentNotes = it },
                    label = { Text("Payment Notes") },
                    placeholder = { Text("e.g. PayPal, Hand-to-hand, Venmo") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedLabelColor = sportTheme.primary,
                        focusedBorderColor = sportTheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("payment_notes_input")
                )

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
                                onConfirm(memId, amt, paymentNotes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = sportTheme.primary, contentColor = Color.White),
                        modifier = Modifier.testTag("submit_payment_button")
                    ) {
                        Text("Log Payment")
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
    onConfirm: (notes: String, dateMillis: Long, expenses: List<ExpenseItem>, attendanceList: List<Attendance>) -> Unit
) {
    var notes by remember { mutableStateOf("") }

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
                                    onConfirm(notes, System.currentTimeMillis(), expenseList, finalAttendanceList)
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
