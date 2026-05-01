package net.zygalio.enveloppe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.zygalio.enveloppe.ui.screen.envelopedetail.EnvelopeDetailScreen
import net.zygalio.enveloppe.ui.screen.envelopeedit.EnvelopeEditScreen
import net.zygalio.enveloppe.ui.screen.expenseedit.ExpenseEditScreen
import net.zygalio.enveloppe.ui.screen.home.HomeScreen

private const val HOME = "home"
private const val ENVELOPE_EDIT = "envelope_edit"
private const val ENVELOPE_DETAIL = "envelope_detail"
private const val EXPENSE_EDIT = "expense_edit"

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HOME) {

        composable(HOME) {
            HomeScreen(
                onNewEnvelope = { navController.navigate(ENVELOPE_EDIT) },
                onEnvelopeClick = { id -> navController.navigate("$ENVELOPE_DETAIL/$id") },
            )
        }

        composable(
            route = "$ENVELOPE_EDIT?envelopeId={envelopeId}",
            arguments = listOf(navArgument("envelopeId") {
                type = NavType.LongType
                defaultValue = -1L
            }),
        ) {
            EnvelopeEditScreen(
                onBack = { navController.popBackStack() },
                onDuplicated = { newId -> navController.navigate("$ENVELOPE_DETAIL/$newId") },
            )
        }

        composable(
            route = "$ENVELOPE_DETAIL/{envelopeId}",
            arguments = listOf(navArgument("envelopeId") { type = NavType.LongType }),
        ) {
            EnvelopeDetailScreen(
                onBack = { navController.popBackStack() },
                onEditEnvelope = { id ->
                    navController.navigate("$ENVELOPE_EDIT?envelopeId=$id")
                },
                onNewExpense = { envelopeId ->
                    navController.navigate("$EXPENSE_EDIT/$envelopeId")
                },
                onEditExpense = { envelopeId, expenseId ->
                    navController.navigate("$EXPENSE_EDIT/$envelopeId?expenseId=$expenseId")
                },
            )
        }

        composable(
            route = "$EXPENSE_EDIT/{envelopeId}?expenseId={expenseId}",
            arguments = listOf(
                navArgument("envelopeId") { type = NavType.LongType },
                navArgument("expenseId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) {
            ExpenseEditScreen(onBack = { navController.popBackStack() })
        }
    }
}
