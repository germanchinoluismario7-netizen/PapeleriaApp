package com.example.papeleriaapp.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.papeleriaapp.presentation.cart.CartScreen
import com.example.papeleriaapp.presentation.customerHome.CustomerHomeScreen
import com.example.papeleriaapp.presentation.login.LoginScreen
import com.example.papeleriaapp.presentation.login.LoginViewModel
import com.example.papeleriaapp.presentation.orders.OrdersScreen
import com.example.papeleriaapp.presentation.products.ProductFormScreen
import com.example.papeleriaapp.presentation.products.SellerProductsScreen
import com.example.papeleriaapp.presentation.registerCustomer.RegisterCustomerScreen
import com.example.papeleriaapp.presentation.registerCustomer.RegisterCustomerViewModel
import com.example.papeleriaapp.presentation.registerSeller.RegisterSellerScreen
import com.example.papeleriaapp.presentation.registerSeller.RegisterSellerViewModel
import com.example.papeleriaapp.presentation.registerStore.RegisterStoreScreen
import com.example.papeleriaapp.presentation.registerStore.RegisterStoreViewModel
import com.example.papeleriaapp.presentation.selectRole.SelectRoleScreen
import com.example.papeleriaapp.presentation.sellerHome.SellerHomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController
){
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route
    ){
        //Login (con sesion guardada y navegacion por rol)
        composable(AppRoute.Login.route) {
            val viewModel : LoginViewModel = hiltViewModel()

            LoginScreen(
                viewModel = viewModel,
                onGoToSelectRole = {
                    navController.navigate(AppRoute.SelectRole.route)
                },
                onLoginCustomer = {
                    navController.navigate(AppRoute.CustomerHome.route){
                        popUpTo(AppRoute.Login.route)
                    }
                },
                onLoginSeller = {
                    navController.navigate(AppRoute.SellerHome.route){
                        popUpTo(AppRoute.Login.route)
                    }
                },
                onLoginSellerWithoutStore = { sellerUid ->
                    navController.navigate(AppRoute.registerStore(sellerUid)){
                        popUpTo(AppRoute.Login.route)
                    }
                }
            )
        }

        composable(AppRoute.SelectRole.route) {
            SelectRoleScreen(
                onGoToRegisterSeller = {
                    navController.navigate(AppRoute.RegisterSeller.route)
                },
                onGoToRegisterCustomer = {
                    navController.navigate(AppRoute.RegisterCustomer.route)
                }
            )
        }

        //Registro del vendedor -> registro de la papeleria
        composable(AppRoute.RegisterSeller.route) {
            val viewModel : RegisterSellerViewModel = hiltViewModel()

            RegisterSellerScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onFinishRegisterSeller = { sellerUid ->
                    navController.navigate(AppRoute.registerStore(sellerUid))
                }
            )
        }

        composable(
            route = AppRoute.RegisterStore.route,
            arguments = listOf(
                navArgument("sellerUid") {
                    type = NavType.StringType
                }
            )
        ) {
            val viewModel : RegisterStoreViewModel = hiltViewModel()

            RegisterStoreScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onFinishRegisterStore = {
                    navController.navigate(AppRoute.SellerHome.route){
                        popUpTo(AppRoute.Login.route){
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AppRoute.RegisterCustomer.route) {
            val viewModel : RegisterCustomerViewModel = hiltViewModel()

            RegisterCustomerScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.navigate(AppRoute.CustomerHome.route){
                        popUpTo(AppRoute.Login.route)
                    }
                }
            )
        }

        //Cliente: catalogo de papeleria
        composable(AppRoute.CustomerHome.route){
            CustomerHomeScreen(
                onGoToCart = {
                    navController.navigate(AppRoute.Cart.route)
                },
                onGoToOrders = {
                    navController.navigate(AppRoute.Orders.route)
                },
                onLogout = {
                    navController.navigate(AppRoute.Login.route){
                        popUpTo(AppRoute.Login.route){
                            inclusive = true
                        }
                    }
                }
            )
        }

        //Cliente: carrito de compras
        composable(AppRoute.Cart.route){
            CartScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOrderCreated = {
                    navController.navigate(AppRoute.Orders.route){
                        popUpTo(AppRoute.Cart.route){
                            inclusive = true
                        }
                    }
                }
            )
        }

        //Cliente: mis pedidos
        composable(AppRoute.Orders.route){
            OrdersScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        //Vendedor: pedidos de la papeleria
        composable(AppRoute.SellerHome.route){
            SellerHomeScreen(
                onGoToProducts = {
                    navController.navigate(AppRoute.SellerProducts.route)
                },
                onLogout = {
                    navController.navigate(AppRoute.Login.route){
                        popUpTo(AppRoute.Login.route){
                            inclusive = true
                        }
                    }
                }
            )
        }

        //Vendedor: CRUD de productos
        composable(AppRoute.SellerProducts.route){
            SellerProductsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAddProduct = {
                    navController.navigate(AppRoute.productForm())
                },
                onEditProduct = { productId ->
                    navController.navigate(AppRoute.productForm(productId))
                }
            )
        }

        //Vendedor: formulario crear / editar producto
        composable(
            route = AppRoute.ProductForm.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ){
            ProductFormScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}
