package com.example.papeleriaapp.presentation.registerSeller

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.papeleriaapp.core.model.AppUser
import com.example.papeleriaapp.core.model.UserRole
import com.example.papeleriaapp.domain.repository.AuthRepository
import com.example.papeleriaapp.ui.theme.PapeleriaAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class RegisterSellerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Repositorio falso para poder construir el ViewModel en los tests. */
    private class FakeAuthRepository : AuthRepository {
        override suspend fun registerUser(
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            phone: String,
            role: UserRole
        ): Result<AppUser> = Result.success(AppUser(uid = "uid-test"))

        override suspend fun login(email: String, password: String): Result<AppUser> =
            Result.success(AppUser(uid = "uid-test"))

        override suspend fun getCurrentUser(): AppUser? = null

        override fun logout() {}
    }

    private fun buildViewModel(): RegisterSellerViewModel =
        RegisterSellerViewModel(FakeAuthRepository())

    @Test
    fun registerSellerScreen_displaysTitle() {
        composeTestRule.setContent {
            PapeleriaAppTheme {
                RegisterSellerScreen(
                    viewModel = buildViewModel(),
                    onBack = {},
                    onFinishRegisterSeller = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Registro del Vendedor")
            .assertIsDisplayed()
    }

    @Test
    fun registerSellerScreen_displaysBackButton() {
        composeTestRule.setContent {
            PapeleriaAppTheme {
                RegisterSellerScreen(
                    viewModel = buildViewModel(),
                    onBack = {},
                    onFinishRegisterSeller = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Volver")
            .assertIsDisplayed()
    }

    @Test
    fun registerSellerScreen_displaysImage() {
        composeTestRule.setContent {
            PapeleriaAppTheme {
                RegisterSellerScreen(
                    viewModel = buildViewModel(),
                    onBack = {},
                    onFinishRegisterSeller = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Registrarme como Vendedor")
            .assertIsDisplayed()
    }

    @Test
    fun registerSellerScreen_clickBackButton_triggersOnBackCallback() {
        var backClicked = false

        composeTestRule.setContent {
            PapeleriaAppTheme {
                RegisterSellerScreen(
                    viewModel = buildViewModel(),
                    onBack = { backClicked = true },
                    onFinishRegisterSeller = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Volver")
            .performClick()

        assertTrue("Expected onBack callback to be triggered", backClicked)
    }
}
