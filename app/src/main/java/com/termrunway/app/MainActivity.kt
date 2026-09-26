package com.termrunway.app

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import kotlin.math.max

data class Expense(val id:String=UUID.randomUUID().toString(),val amount:Long,val category:String,val date:Long)
data class Income(val id:String=UUID.randomUUID().toString(),val amount:Long,val source:String,val date:Long)
data class Plan(val start:Long,val end:Long,val available:Long,val expectedIncome:Long,val expectedExpense:Long)
data class AppState(val name:String="",val dailyLimit:Long=0L,val expenses:List<Expense> = emptyList(),val incomes:List<Income> = emptyList(),val plan:Plan?=null)
enum class Mode{DAILY,PLAN}
enum class DailyTab{HOME,TRACK,INSIGHTS}
enum class PlanTab{OVERVIEW,PLAN,RUNWAY,INSIGHTS}

private const val PREFS="termrunway_reference"
private const val KEY="state"
private const val DAY=86_400_000L
private val Night=Color(0xFF070B12)
private val Panel=Color(0xFF101722)
private val Panel2=Color(0xFF162131)
private val Blue=Color(0xFF67AEFF)
private val Green=Color(0xFF5ED39B)
private val Red=Color(0xFFFF6B7A)
private val Muted=Color(0xFFA6B8CA)

private val Dark=darkColorScheme(
    primary=Blue,onPrimary=Color(0xFF07111D),secondary=Green,onSecondary=Color(0xFF07130D),
    background=Night,onBackground=Color.White,surface=Panel,onSurface=Color.White,
    surfaceVariant=Panel2,onSurfaceVariant=Muted,error=Red,errorContainer=Color(0xFF4A1D25)
)
private val Light=lightColorScheme(
    primary=Color(0xFF1769D2),secondary=Color(0xFF16845B),background=Color(0xFFF6F9FC),
    onBackground=Color(0xFF101722),surface=Color.White,onSurface=Color(0xFF101722),
    surfaceVariant=Color(0xFFEAF0F7),onSurfaceVariant=Color(0xFF5D6C7E),error=Red
)

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{TermRunwayApp()}
    }
}

@Composable
fun TermRunwayApp(){
    val context=LocalContext.current.applicationContext
    var state by remember{mutableStateOf(loadState(context))}
    var dark by rememberSaveable{mutableStateOf(true)}
    var mode by rememberSaveable{mutableStateOf(Mode.DAILY)}
    var dailyTab by rememberSaveable{mutableStateOf(DailyTab.HOME)}
    var planTab by rememberSaveable{mutableStateOf(PlanTab.OVERVIEW)}
    var settingsOpen by rememberSaveable{mutableStateOf(false)}
    var editor by remember{mutableStateOf<EditorArgs?>(null)}

    fun save(s:AppState){state=s;saveState(context,s)}

    MaterialTheme(colorScheme=if(dark)Dark else Light){
        when{
            state.name.isBlank()->SetupScreen{save(state.copy(name=it))}
            settingsOpen->SettingsScreen(state,dark,{dark=it},{settingsOpen=false},{save(state.copy(name=it))},{save(state.copy(dailyLimit=it))})
            else->Scaffold(
                bottomBar={if(mode==Mode.DAILY)DailyBar(dailyTab){dailyTab=it}else PlanBar(planTab){planTab=it}},
                floatingActionButton={if(mode==Mode.DAILY&&dailyTab==DailyTab.TRACK)FloatingActionButton({editor=EditorArgs(true)}){Icon(Icons.Outlined.Add,"Add transaction")}}
            ){pad->
                if(mode==Mode.DAILY){
                    when(dailyTab){
                        DailyTab.HOME->DailyHome(state,pad,{settingsOpen=true},{mode=Mode.PLAN;planTab=PlanTab.OVERVIEW},{dailyTab=DailyTab.TRACK},{dailyTab=DailyTab.INSIGHTS},{editor=EditorArgs(true)},{editor=EditorArgs(false)}, {editor=EditorArgs(true,it)})
                        DailyTab.TRACK->DailyTrack(state,pad,{settingsOpen=true},{editor=EditorArgs(true)},{editor=EditorArgs(true,it)},{editor=EditorArgs(false,null,it)})
                        DailyTab.INSIGHTS->DailyInsights(state,pad){settingsOpen=true}
                    }
                }else{
                    when(planTab){
                        PlanTab.OVERVIEW->PlanOverview(state,pad,{settingsOpen=true},{mode=Mode.DAILY},{planTab=it})
                        PlanTab.PLAN->PlanEditor(state,pad,{settingsOpen=true},{mode=Mode.DAILY}){save(state.copy(plan=it));planTab=PlanTab.OVERVIEW}
                        PlanTab.RUNWAY->Runway(state,pad){settingsOpen=true}
                        PlanTab.INSIGHTS->PlanInsights(state,pad){settingsOpen=true}
                    }
                }
            }
            editor?.let{TransactionSheet(it,state,{editor=null}){save(it);editor=null}}
        }
    }
}

data class EditorArgs(val expenseMode:Boolean,val expense:Expense?=null,val income:Income?=null)

@Composable
private fun SetupScreen(onDone:(String)->Unit){
    var name by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),verticalArrangement=Arrangement.Center){
        Brand()
        Text("Welcome to TermRunway",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=24.dp))
        Text("PLAN • CALCULATE • ACHIEVE",color=Blue,modifier=Modifier.padding(top=6.dp))
        Text("Private, offline money tracking and term planning.",color=Muted,modifier=Modifier.padding(top=12.dp))
        OutlinedTextField(name,{name=it.take(32)},Modifier.fillMaxWidth().padding(top=24.dp),label={Text("Your name")},singleLine=true)
        Button(onClick={onDone(name.trim())},enabled=name.isNotBlank(),modifier=Modifier.fillMaxWidth().padding(top=14.dp)){Text("Enter TermRunway")}
    }
}

@Composable private fun Brand(){
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF0D1B2A)),contentAlignment=Alignment.Center){
            Canvas(Modifier.size(28.dp)){
                val path=Path().apply{moveTo(2f,22f);lineTo(9f,14f);lineTo(15f,18f);lineTo(25f,5f)}
                drawPath(path,style=Stroke(4f),color=Blue);drawLine(Green,Offset(18f,11f),Offset(25f,5f),4f)
            }
        }
        Text("TERM RUNWAY",fontWeight=FontWeight.Bold,letterSpacing=1.1.sp)
    }
}

@Composable private fun DailyBar(selected:DailyTab,onSelect:(DailyTab)->Unit){
    NavigationBar{
        NavigationBarItem(selected==DailyTab.HOME,{onSelect(DailyTab.HOME)},{Icon(Icons.Outlined.Home,"Home")},label={Text("Home")})
        NavigationBarItem(selected==DailyTab.TRACK,{onSelect(DailyTab.TRACK)},{Icon(Icons.Outlined.List,"Track")},label={Text("Track")})
        NavigationBarItem(selected==DailyTab.INSIGHTS,{onSelect(DailyTab.INSIGHTS)},{Icon(Icons.Outlined.AutoGraph,"Insights")},label={Text("Insights")})
    }
}
@Composable private fun PlanBar(selected:PlanTab,onSelect:(PlanTab)->Unit){
    NavigationBar{
        NavigationBarItem(selected==PlanTab.OVERVIEW,{onSelect(PlanTab.OVERVIEW)},{Icon(Icons.Outlined.Home,"Overview")},label={Text("Overview")})
        NavigationBarItem(selected==PlanTab.PLAN,{onSelect(PlanTab.PLAN)},{Icon(Icons.Outlined.Tune,"Plan")},label={Text("Plan")})
        NavigationBarItem(selected==PlanTab.RUNWAY,{onSelect(PlanTab.RUNWAY)},{Icon(Icons.Outlined.AutoGraph,"Runway")},label={Text("Runway")})
        NavigationBarItem(selected==PlanTab.INSIGHTS,{onSelect(PlanTab.INSIGHTS)},{Icon(Icons.Outlined.Info,"Insights")},label={Text("Insights")})
    }
}

@Composable private fun Header(title:String,subtitle:String,onSettings:()->Unit){
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
        Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(subtitle,color=Muted,modifier=Modifier.padding(top=3.dp))}
        IconButton(onSettings){Icon(Icons.Outlined.Settings,"Settings")}
    }
}

@Composable private fun ModeSwitcher(plan:Boolean,onDaily:()->Unit,onPlan:()->Unit){
    Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Panel2)){
        Row(Modifier.fillMaxWidth().padding(4.dp)){
            ModeChip("Daily Tracking",!plan,onDaily,Modifier.weight(1f))
            ModeChip("Plan Tracking",plan,onPlan,Modifier.weight(1f))
        }
    }
}
@Composable private fun ModeChip(label:String,selected:Boolean,onClick:()->Unit,modifier:Modifier){
    Card(modifier=modifier,onClick=onClick,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=if(selected)Blue else Color.Transparent),elevation=CardDefaults.cardElevation(defaultElevation=0.dp)){
        Text(label,Modifier.fillMaxWidth().padding(vertical=11.dp),textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontWeight=FontWeight.SemiBold,color=if(selected)Color(0xFF07111D) else Muted)
    }
}

@Composable private fun DailyHome(s:AppState,p:PaddingValues,onSettings:()->Unit,onPlan:()->Unit,onTrack:()->Unit,onInsights:()->Unit,onExpense:()->Unit,onIncome:()->Unit,onEdit:(Expense)->Unit){
    val today=startOfDay(System.currentTimeMillis());val spent=s.expenses.filter{sameDay(it.date,today)}.sumOf{it.amount};val income=s.incomes.filter{sameDay(it.date,today)}.sumOf{it.amount}
    val recent=s.expenses.sortedByDescending{it.date}.take(5)
    LazyColumn(contentPadding=PaddingValues(18.dp,p.calculateTopPadding()+10.dp,18.dp,p.calculateBottomPadding()+24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Header("Daily Tracking","What actually happened",onSettings)}
        item{ModeSwitcher(false,{},onPlan)}
        item{Text("Good to see you, "+s.name,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold)}
        item{Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Panel2)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Row(Modifier.fillMaxWidth()){Text("TODAY",color=Blue,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("ACTUAL",color=Muted)}
            Text(money(spent),style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);Text("spent today",color=Muted)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Income",money(income),Modifier.weight(1f));Metric("Net",money(income-spent),Modifier.weight(1f))}
            if(s.dailyLimit>0){val left=s.dailyLimit-spent;LinearProgressIndicator({(spent.toFloat()/s.dailyLimit.coerceAtLeast(1)).coerceIn(0f,1f)},Modifier.fillMaxWidth());Text(if(left>=0)money(left)+" left of daily reference" else money(-left)+" above daily reference",color=if(left<0)Red else Muted)}else Text("No budget required in Daily Tracking.",color=Muted)
        }}}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){ActionCard("Add expense",onExpense,Modifier.weight(1f));ActionCard("Add income",onIncome,Modifier.weight(1f))}}
        item{PulseCard(s)}
        item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Recent activity",fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));TextButton(onTrack){Text("See all")}}}
        if(recent.isEmpty()) item{EmptyState("Nothing recorded yet","Add a transaction to start your actual history.")} else items(recent){expense->TxRow(expense.id,expense.category,expense.amount,false,expense.date){onEdit(expense)}}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){TextButton(onTrack,Modifier.weight(1f)){Text("Open Track")};TextButton(onInsights,Modifier.weight(1f)){Text("Open Insights")}}}
    }
}

@Composable private fun DailyTrack(s:AppState,p:PaddingValues,onSettings:()->Unit,onAdd:()->Unit,onEditExpense:(Expense)->Unit,onEditIncome:(Income)->Unit){
    val context=LocalContext.current;var day by rememberSaveable{mutableLongStateOf(startOfDay(System.currentTimeMillis()))}
    val ex=s.expenses.filter{sameDay(it.date,day)}.sortedByDescending{it.date};val inc=s.incomes.filter{sameDay(it.date,day)}.sortedByDescending{it.date}
    LazyColumn(contentPadding=PaddingValues(18.dp,p.calculateTopPadding()+10.dp,18.dp,p.calculateBottomPadding()+96.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Header("Track","Daily transaction history",onSettings)}
        item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            IconButton({day=addDays(day,-1)}){Icon(Icons.Outlined.ArrowBack,"Previous")}
            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Text(if(isToday(day))"Today" else prettyDate(day),fontWeight=FontWeight.SemiBold);Text(prettyDate(day),style=MaterialTheme.typography.labelMedium,color=Muted)}
            IconButton({day=addDays(day,1)},enabled=!isToday(day)){Icon(Icons.Outlined.ArrowForward,"Next")}
            IconButton({pickDate(context,day){day=it}}){Icon(Icons.Outlined.CalendarMonth,"Calendar")}
        }}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Spent",money(ex.sumOf{it.amount}),Modifier.weight(1f));Metric("Income",money(inc.sumOf{it.amount}),Modifier.weight(1f));Metric("Net",money(inc.sumOf{it.amount}-ex.sumOf{it.amount}),Modifier.weight(1f))}}
        if(ex.isEmpty()&&inc.isEmpty())item{EmptyState("No transactions","Record what actually came in or went out.","Add transaction",onAdd)}
        else{
            if(inc.isNotEmpty()){item{Text("Income",fontWeight=FontWeight.SemiBold)};items(inc,key={it.id}){item->TxRow(item.id,item.source,item.amount,true,item.date){onEditIncome(item)}}}
            if(ex.isNotEmpty()){item{Text("Expenses",fontWeight=FontWeight.SemiBold)};items(ex,key={it.id}){item->TxRow(item.id,item.category,item.amount,false,item.date){onEditExpense(item)}}}
        }
    }
}

@Composable private fun DailyInsights(s:AppState,p:PaddingValues,onSettings:()->Unit){
    var days by rememberSaveable{mutableLongStateOf(7)};val today=startOfDay(System.currentTimeMillis());val start=addDays(today,-(days.toInt()-1))
    val ex=s.expenses.filter{it.date in start..endOfDay(today)};val inc=s.incomes.filter{it.date in start..endOfDay(today)}
    LazyColumn(contentPadding=PaddingValues(18.dp,p.calculateTopPadding()+10.dp,18.dp,p.calculateBottomPadding()+24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Header("Insights","Spending & income",onSettings)}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(7L,30L,90L).forEach{n->FilterChip(days==n,{days=n},label={Text(n.toString()+"d")})}}}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Income",money(inc.sumOf{it.amount}),Modifier.weight(1f));Metric("Expense",money(ex.sumOf{it.amount}),Modifier.weight(1f));Metric("Net",money(inc.sumOf{it.amount}-ex.sumOf{it.amount}),Modifier.weight(1f))}}
        item{ChartCard(s,start,today)}
        item{Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Spending by category",fontWeight=FontWeight.SemiBold);val cats=ex.groupBy{it.category}.mapValues{(_,v)->v.sumOf{it.amount}}.toList().sortedByDescending{it.second};if(cats.isEmpty())Text("No expense data yet.",color=Muted)else cats.take(6).forEach{(cat,a)->CategoryBar(cat,a,cats.maxOf{it.second})}}}}
    }
}

@Composable private fun PlanOverview(s:AppState,p:PaddingValues,onSettings:()->Unit,onDaily:()->Unit,onTab:(PlanTab)->Unit){
    val plan=s.plan
    LazyColumn(contentPadding=PaddingValues(18.dp,p.calculateTopPadding()+10.dp,18.dp,p.calculateBottomPadding()+24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Header("Plan Tracking","What will happen",onSettings)}
        item{ModeSwitcher(true,onDaily,{})}
        if(plan==null)item{Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Panel2)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Build your first term plan",style=MaterialTheme.typography.headlineSmall);Text("Set dates, money available now, expected income and expected expenses.",color=Muted);Button(onClick={onTab(PlanTab.PLAN)}){Text("Create plan")}}}}
        else{val m=planSummary(s,plan);item{Card(shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=Panel2)){Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Row(Modifier.fillMaxWidth()){Column(Modifier.weight(1f)){Text("RUNWAY",color=Blue,fontWeight=FontWeight.Bold);Text(money(m.current),style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);Text("current balance",color=Muted)}StatusChip(m.status)}LinearProgressIndicator({m.progress},Modifier.fillMaxWidth());Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Days left",m.daysRemaining.toString(),Modifier.weight(1f));Metric("Safe / day",money(m.safeDay),Modifier.weight(1f))};Text("Projected end "+money(m.projected),fontWeight=FontWeight.SemiBold)}}};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Expected income",money(m.expectedIncome),Modifier.weight(1f));Metric("Planned expense",money(m.plannedExpense),Modifier.weight(1f))}};item{Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Text("Timeline",fontWeight=FontWeight.SemiBold);Text(prettyDate(plan.start)+" → "+prettyDate(plan.end));Text(m.totalDays.toString()+" days · "+money(plan.available)+" available now",color=Muted)}}};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={onTab(PlanTab.PLAN)},Modifier.weight(1f)){Text("Edit plan")};Button(onClick={onTab(PlanTab.RUNWAY)},Modifier.weight(1f)){Text("Runway")}}}}
    }
}

data class PlanSummary(val totalDays:Int,val daysRemaining:Int,val current:Long,val expectedIncome:Long,val plannedExpense:Long,val safeDay:Long,val projected:Long,val progress:Float,val status:String)
private fun planSummary(s:AppState,p:Plan):PlanSummary{
    val now=startOfDay(System.currentTimeMillis());val total=daysInclusive(p.start,p.end).coerceAtLeast(1);val elapsed=if(now<=p.start)0 else daysInclusive(p.start,now.coerceAtMost(p.end)).coerceIn(0,total);val remain=(total-elapsed).coerceAtLeast(0)
    val actualE=s.expenses.filter{it.date in p.start..endOfDay(p.end)}.sumOf{it.amount};val actualI=s.incomes.filter{it.date in p.start..endOfDay(p.end)}.sumOf{it.amount};val futureIncome=(p.expectedIncome-actualI).coerceAtLeast(0);val futureExpense=(p.expectedExpense-actualE).coerceAtLeast(0)
    val current=p.available+actualI-actualE;val projected=current+futureIncome-futureExpense;val safe=if(remain==0)0 else max(current+futureIncome,0)/remain;val planToDate=(p.expectedExpense.toDouble()*elapsed/total).toLong()
    val status=when{projected<0->"Needs work";planToDate>0&&actualE>(planToDate*1.15).toLong()->"Above plan";else->"On track"}
    return PlanSummary(total,remain,current,p.expectedIncome,p.expectedExpense,safe,projected,elapsed.toFloat()/total,status)
}

@Composable private fun PlanEditor(s:AppState,p:PaddingValues,onSettings:()->Unit,onDaily:()->Unit,onSave:(Plan)->Unit){
    val context=LocalContext.current;val existing=s.plan;val today=startOfDay(System.currentTimeMillis())
    var start by remember(existing){mutableLongStateOf(existing?.start?:today)};var end by remember(existing){mutableLongStateOf(existing?.end?:addDays(today,89))}
    var available by remember(existing){mutableStateOf(existing?.available?.let(::moneyInput)?:"")};var expectedIncome by remember(existing){mutableStateOf(existing?.expectedIncome?.let(::moneyInput)?:"")};var expectedExpense by remember(existing){mutableStateOf(existing?.expectedExpense?.let(::moneyInput)?:"")}
    val av=parseMoney(available);val inc=parseMoney(expectedIncome);val exp=parseMoney(expectedExpense);val projected=av+inc-exp;val totalDays=daysInclusive(start,end)
    LazyColumn(contentPadding=PaddingValues(18.dp,p.calculateTopPadding()+10.dp,18.dp,p.calculateBottomPadding()+24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Header("Plan","Shape the term before you live it",onSettings)};item{ModeSwitcher(true,onDaily,{})}
        item{Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=if(projected<0)Color(0xFF28131A) else Color(0xFF12352D))){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Live plan math",fontWeight=FontWeight.SemiBold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Available",money(av),Modifier.weight(1f));Metric("Income",money(inc),Modifier.weight(1f));Metric("Expense",money(exp),Modifier.weight(1f))};Text("Projected left "+money(projected),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,color=if(projected<0)Red else Green);Text(if(projected<0)"This plan is not sufficient yet. Reduce expenses or increase income." else totalDays.toString()+" days · planned pace "+money(if(totalDays>0)exp/totalDays else 0)+" per day",color=Muted)}}}
        item{Text("Term dates",fontWeight=FontWeight.SemiBold)};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={pickDate(context,start){start=it}},Modifier.weight(1f)){Text("From\n"+prettyDate(start))};OutlinedButton(onClick={pickDate(context,end){end=it}},Modifier.weight(1f)){Text("Until\n"+prettyDate(end))}}}
        item{Text("Money available now",fontWeight=FontWeight.SemiBold)};item{MoneyField(available,{available=it},"Available money")}
        item{Text("Expected income",fontWeight=FontWeight.SemiBold)};item{MoneyField(expectedIncome,{expectedIncome=it},"Parent + scholarship + work")}
        item{Text("Expected expenses",fontWeight=FontWeight.SemiBold)};item{MoneyField(expectedExpense,{expectedExpense=it},"Total planned expenses")}
        item{Button(enabled=end>=start,onClick={onSave(Plan(start,end,av,inc,exp))},Modifier.fillMaxWidth()){Text(if(existing==null)"Save term plan" else "Update term plan")}}
        item{Text("Everything stays on this device. No account or network is used.",style=MaterialTheme.typography.labelMedium,color=Muted)}
    }
}

@Composable private fun Runway(s:AppState,p:PaddingValues,onSettings:()->Unit){
    LazyColumn(contentPadding=PaddingValues(18.dp,p.calculateTopPadding()+10.dp,18.dp,p.calculateBottomPadding()+24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Header("Runway","What your current pace means",onSettings)}
        if(s.plan==null)item{EmptyState("No term plan yet","Create a plan and Runway turns it into a live forecast.")} else {val m=planSummary(s,s.plan);item{Card(shape=RoundedCornerShape(30.dp),colors=CardDefaults.cardColors(containerColor=Panel2)){Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("RUNWAY",color=Blue,fontWeight=FontWeight.Bold);Text(money(m.current),style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);Text("current balance",color=Muted);LinearProgressIndicator({m.progress},Modifier.fillMaxWidth());Text(m.daysRemaining.toString()+" days remaining",fontWeight=FontWeight.SemiBold)}}};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Safe / day",money(m.safeDay),Modifier.weight(1f));Metric("Planned / day",money(m.plannedExpense/m.totalDays.coerceAtLeast(1)),Modifier.weight(1f))}};item{Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text("Runway outlook",fontWeight=FontWeight.SemiBold);Text(when(m.status){"Needs work"->"Your plan does not fully cover expected spending.";"Above plan"->"Actual spending is running ahead of the plan-to-date pace.";else->"Your current pace is inside the plan."});Text("Projected end "+money(m.projected),color=if(m.projected<0)Red else Green,fontWeight=FontWeight.SemiBold)}}}}
    }
}

@Composable private fun PlanInsights(s:AppState,p:PaddingValues,onSettings:()->Unit){
    LazyColumn(contentPadding=PaddingValues(18.dp,p.calculateTopPadding()+10.dp,18.dp,p.calculateBottomPadding()+24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Header("Term Insights","Plan versus reality",onSettings)}
        if(s.plan==null)item{EmptyState("Plan insights need a plan","Create a term plan and actual transactions will be compared against it.")}else{val m=planSummary(s,s.plan);item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Expected income",money(m.expectedIncome),Modifier.weight(1f));Metric("Planned expense",money(m.plannedExpense),Modifier.weight(1f));Metric("Projected",money(m.projected),Modifier.weight(1f))}};item{PlanCompare("Planned expense",m.plannedExpense,s.expenses.filter{it.date in s.plan.start..endOfDay(s.plan.end)}.sumOf{it.amount})}}
    }
}

@Composable private fun SettingsScreen(s:AppState,dark:Boolean,onDark:(Boolean)->Unit,onBack:()->Unit,onName:(String)->Unit,onLimit:(Long)->Unit){
    var name by remember(s.name){mutableStateOf(s.name)};var limit by remember(s.dailyLimit){mutableStateOf(if(s.dailyLimit>0)moneyInput(s.dailyLimit) else "")}
    LazyColumn(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onBack){Icon(Icons.Outlined.ArrowBack,"Back")};Text("Settings",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}}
        item{Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Profile",fontWeight=FontWeight.SemiBold);OutlinedTextField(name,{name=it.take(32)},Modifier.fillMaxWidth(),label={Text("Name")},singleLine=true);Button({onName(name.trim())}){Text("Save name")}}}}
        item{Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp)){Text("Theme",fontWeight=FontWeight.SemiBold);Row(verticalAlignment=Alignment.CenterVertically){RadioButton(dark,{onDark(true)});Text("Dark")};Row(verticalAlignment=Alignment.CenterVertically){RadioButton(!dark,{onDark(false)});Text("Light")}}}}
        item{Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Daily reference (optional)",fontWeight=FontWeight.SemiBold);Text("Tracking mode never blocks a transaction.",color=Muted);MoneyField(limit,{limit=it},"Daily limit");Button({onLimit(parseMoney(limit))}){Text("Save reference")}}}}
        item{Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Panel2)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Offline by design",fontWeight=FontWeight.SemiBold);Text("Only your name is requested. Financial data stays on this device.",color=Muted)}}}
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable private fun TransactionSheet(args:EditorArgs,state:AppState,onClose:()->Unit,onSave:(AppState)->Unit){
    val context=LocalContext.current;val sheet=rememberModalBottomSheetState(skipPartiallyExpanded=true);var isExpense by remember(args){mutableStateOf(args.expense!=null||(args.income==null&&args.expenseMode))}
    var amount by remember(args){mutableStateOf(args.expense?.amount?.let(::moneyInput)?:args.income?.amount?.let(::moneyInput)?:"")};var category by remember(args){mutableStateOf(args.expense?.category?:"Food")};var source by remember(args){mutableStateOf(args.income?.source?:"Parent support")};var day by remember(args){mutableLongStateOf(startOfDay(args.expense?.date?:args.income?.date?:System.currentTimeMillis()))}
    ModalBottomSheet(onDismissRequest=onClose,sheetState=sheet){Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(if(args.expense!=null||args.income!=null)"Edit transaction" else "Add transaction",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(args.expense==null&&args.income==null)Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(isExpense,{isExpense=true},{Text("Expense")});FilterChip(!isExpense,{isExpense=false},{Text("Income")})};MoneyField(amount,{amount=it},"Amount");if(isExpense)OutlinedTextField(category,{category=it},Modifier.fillMaxWidth(),label={Text("Category")},singleLine=true)else OutlinedTextField(source,{source=it},Modifier.fillMaxWidth(),label={Text("Income source")},singleLine=true);OutlinedButton({pickDate(context,day){day=it}},Modifier.fillMaxWidth()){Text("Date "+prettyDate(day))};Button({val cents=parseMoney(amount);if(cents>0){if(isExpense)onSave(state.copy(expenses=state.expenses.filterNot{it.id==args.expense?.id}.plus(Expense(args.expense?.id?:UUID.randomUUID().toString(),cents,category,day))))else onSave(state.copy(incomes=state.incomes.filterNot{it.id==args.income?.id}.plus(Income(args.income?.id?:UUID.randomUUID().toString(),cents,source,day))))}},Modifier.fillMaxWidth()){Text("Save transaction")}}}
}

@Composable private fun Metric(label:String,value:String,modifier:Modifier=Modifier){Card(modifier,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Panel)){Column(Modifier.padding(14.dp)){Text(label,color=Muted,style=MaterialTheme.typography.labelMedium);Text(value,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)}}}
@Composable private fun ActionCard(title:String,onClick:()->Unit,modifier:Modifier){
    Card(
        modifier=modifier.clickable(onClick=onClick),
        shape=RoundedCornerShape(20.dp),
        colors=CardDefaults.cardColors(containerColor=Panel2)
    ){
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
            Icon(Icons.Outlined.Add,null,tint=Blue)
            Text(title,fontWeight=FontWeight.SemiBold,Modifier.padding(start=8.dp))
        }
    }
}
@Composable private fun TxRow(id:String,title:String,amount:Long,income:Boolean,date:Long,onClick:()->Unit){Card(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.SemiBold);Text(prettyDate(date),style=MaterialTheme.typography.labelMedium,color=Muted)};Text((if(income)"+" else "-")+money(amount),color=if(income)Green else Red,fontWeight=FontWeight.SemiBold)}}}
@Composable private fun EmptyState(title:String,description:String,action:String?=null,onAction:(()->Unit)?=null){
    Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Text(title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold)
        Text(description,color=Muted,Modifier.padding(top=6.dp))
        if(action!=null&&onAction!=null) TextButton(onClick=onAction){Text(action)}
    }
}
@Composable private fun PulseCard(s:AppState){val t=startOfDay(System.currentTimeMillis());val ds=(6 downTo 0).map{addDays(t,-it)};val p=ds.map{d->Pair(s.expenses.filter{sameDay(it.date,d)}.sumOf{it.amount},s.incomes.filter{sameDay(it.date,d)}.sumOf{it.amount})};val mx=p.flatMap{listOf(it.first,it.second)}.maxOrNull()?.coerceAtLeast(1)?:1;Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("7-day pulse",fontWeight=FontWeight.SemiBold);Row(Modifier.fillMaxWidth().height(86.dp),verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(8.dp)){p.forEach{v->Column(Modifier.weight(1f),verticalArrangement=Arrangement.Bottom,horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.width(7.dp).height((v.first.toDouble()/mx*60).toFloat().coerceAtLeast(2f).dp).background(Red,RoundedCornerShape(6.dp)));Spacer(Modifier.height(3.dp));Box(Modifier.width(7.dp).height((v.second.toDouble()/mx*60).toFloat().coerceAtLeast(2f).dp).background(Green,RoundedCornerShape(6.dp)))}}}}}}
@Composable private fun ChartCard(s:AppState,start:Long,end:Long){val days=((end-start)/DAY).toInt()+1;val v=(0 until days).map{n->val d=addDays(start,n);Pair(s.expenses.filter{sameDay(it.date,d)}.sumOf{it.amount},s.incomes.filter{sameDay(it.date,d)}.sumOf{it.amount})};val mx=v.flatMap{listOf(it.first,it.second)}.maxOrNull()?.coerceAtLeast(1)?:1;Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(16.dp)){Text("Spending & Income",fontWeight=FontWeight.SemiBold);Row(Modifier.fillMaxWidth().height(140.dp).horizontalScroll(rememberScrollState()),verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(7.dp)){v.forEach{x->Column(Modifier.width(11.dp)){Box(Modifier.fillMaxWidth().height((x.first.toDouble()/mx*95).toFloat().coerceAtLeast(1f).dp).background(Red,RoundedCornerShape(4.dp)));Spacer(Modifier.height(3.dp));Box(Modifier.fillMaxWidth().height((x.second.toDouble()/mx*95).toFloat().coerceAtLeast(1f).dp).background(Green,RoundedCornerShape(4.dp)))}}};Text("Red = expense · Green = income",style=MaterialTheme.typography.labelSmall,color=Muted)}}}
@Composable private fun CategoryBar(label:String,value:Long,maxValue:Long){Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Row(Modifier.fillMaxWidth()){Text(label,Modifier.weight(1f));Text(money(value),fontWeight=FontWeight.SemiBold)};LinearProgressIndicator({(value.toFloat()/maxValue.coerceAtLeast(1)).coerceIn(0f,1f)},Modifier.fillMaxWidth())}}
@Composable private fun PlanCompare(label:String,planned:Long,actual:Long){val maxValue=max(planned,actual).coerceAtLeast(1);val diff=actual-planned;Card(shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth()){Text(label,Modifier.weight(1f),fontWeight=FontWeight.SemiBold);Text(money(actual))};LinearProgressIndicator({(actual.toDouble()/maxValue).toFloat().coerceIn(0f,1f)},Modifier.fillMaxWidth());Text(if(diff>0)money(diff)+" over" else money(-diff)+" under",color=if(diff>0)Red else Green,style=MaterialTheme.typography.labelMedium)}}}
@Composable private fun StatusChip(status:String){FilterChip(false,{},label={Text(status)})}
@Composable private fun MoneyField(value:String,onValue:(String)->Unit,label:String){OutlinedTextField(value,onValue,Modifier.fillMaxWidth(),label={Text(label+" (₹)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)}

private fun loadState(c:Context):AppState=runCatching{val raw=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(KEY,null)?:return AppState();decode(JSONObject(raw))}.getOrDefault(AppState())
private fun saveState(c:Context,s:AppState){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(KEY,encode(s).toString()).apply()}
private fun encode(s:AppState):JSONObject{
    val expenses=JSONArray()
    s.expenses.forEach{e->expenses.put(JSONObject().put("id",e.id).put("amount",e.amount).put("category",e.category).put("date",e.date))}
    val incomes=JSONArray()
    s.incomes.forEach{i->incomes.put(JSONObject().put("id",i.id).put("amount",i.amount).put("source",i.source).put("date",i.date))}
    val plan=s.plan?.let{p->JSONObject().put("start",p.start).put("end",p.end).put("available",p.available).put("expectedIncome",p.expectedIncome).put("expectedExpense",p.expectedExpense)}
    return JSONObject().put("name",s.name).put("dailyLimit",s.dailyLimit).put("expenses",expenses).put("incomes",incomes).put("plan",plan)
}
private fun decode(o:JSONObject):AppState{
    val expenses=mutableListOf<Expense>()
    val ea=o.optJSONArray("expenses")?:JSONArray()
    for(i in 0 until ea.length()){
        val e=ea.optJSONObject(i)?:continue
        expenses+=Expense(e.optString("id").ifBlank{UUID.randomUUID().toString()},e.optLong("amount"),e.optString("category"),e.optLong("date"))
    }
    val incomes=mutableListOf<Income>()
    val ia=o.optJSONArray("incomes")?:JSONArray()
    for(i in 0 until ia.length()){
        val v=ia.optJSONObject(i)?:continue
        incomes+=Income(v.optString("id").ifBlank{UUID.randomUUID().toString()},v.optLong("amount"),v.optString("source"),v.optLong("date"))
    }
    val plan=o.optJSONObject("plan")?.let{p->Plan(p.optLong("start"),p.optLong("end"),p.optLong("available"),p.optLong("expectedIncome"),p.optLong("expectedExpense"))}
    return AppState(o.optString("name"),o.optLong("dailyLimit"),expenses,incomes,plan)
}
private fun pickDate(c:Context,current:Long,onPicked:(Long)->Unit){val x=Calendar.getInstance().apply{timeInMillis=current};DatePickerDialog(c,{_,y,m,d->onPicked(Calendar.getInstance().apply{set(y,m,d,12,0,0);set(Calendar.MILLISECOND,0)}.timeInMillis)},x.get(Calendar.YEAR),x.get(Calendar.MONTH),x.get(Calendar.DAY_OF_MONTH)).show()}
private fun startOfDay(ms:Long)=Calendar.getInstance().apply{timeInMillis=ms;set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
private fun endOfDay(ms:Long)=startOfDay(ms)+DAY-1
private fun addDays(ms:Long,n:Int)=ms+n*DAY
private fun daysInclusive(a:Long,b:Long)=if(b<a)0 else((startOfDay(b)-startOfDay(a))/DAY).toInt()+1
private fun sameDay(a:Long,b:Long)=startOfDay(a)==startOfDay(b)
private fun isToday(ms:Long)=sameDay(ms,System.currentTimeMillis())
private fun prettyDate(ms:Long)=java.text.SimpleDateFormat("EEE, dd MMM",java.util.Locale.getDefault()).format(java.util.Date(ms))
private fun money(c:Long)="₹"+String.format(java.util.Locale.getDefault(),"%,.0f",c/100.0)
private fun moneyInput(c:Long)=String.format(java.util.Locale.getDefault(),"%.2f",c/100.0)
private fun parseMoney(s:String)=runCatching{(s.trim().toDouble()*100).toLong()}.getOrDefault(0L).coerceAtLeast(0)
