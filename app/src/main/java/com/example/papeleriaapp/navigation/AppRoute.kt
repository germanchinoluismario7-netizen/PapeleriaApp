package com.example.papeleriaapp.navigation

sealed class AppRoute (val route : String){

    data object Login : AppRoute(route = "login")
    data object SelectRole : AppRoute(route = "select_role")
    data object RegisterSeller : AppRoute(route = "register_seller")
    data object RegisterCustomer : AppRoute("register_customer")

    data object RegisterStore : AppRoute(route = "register_store/{sellerUid}")

    data object CustomerHome : AppRoute(route = "customer_home")
    data object Cart : AppRoute(route = "cart")
    data object Orders : AppRoute(route = "orders")

    data object SellerHome : AppRoute(route = "seller_home")

    data object SellerProducts : AppRoute(route = "seller_products")

    data object ProductForm : AppRoute(route = "product_form?productId={productId}")

    companion object {
        fun registerStore(sellerUid : String) : String = "register_store/$sellerUid"

        fun productForm(productId : String? = null) : String =
            if (productId.isNullOrBlank()) "product_form" else "product_form?productId=$productId"
    }
}
