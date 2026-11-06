# Papelería App

App Android (Kotlin + Jetpack Compose + Hilt + Firebase) reestructurada como **tienda de papelería**.

- **Paquete / applicationId:** `com.example.papeleriaapp`
- **Roles:** Vendedor (administra la papelería y sus pedidos) y Cliente (catálogo, carrito y pedidos).
- **Base de datos:** Firebase (Auth + Firestore) del proyecto `multistoreapp-a9203`.

---

## 1. Estructura de la app

| Ruta | Pantalla |
|---|---|
| `login` | Inicio de sesión real (correo/contraseña) + sesión persistente |
| `select_role` | Elegir registrarse como Vendedor o Cliente |
| `register_seller` | Registro del vendedor |
| `register_store/{sellerUid}` | Registro de la papelería (nombre, descripción, categoría) |
| `register_customer` | Registro del cliente |
| `customer_home` | Catálogo de papelería (búsqueda + filtro por categoría) |
| `cart` | Carrito de compras y realización del pedido |
| `orders` | Historial de pedidos del cliente con su estado |
| `seller_home` | Pedidos entrantes de la papelería + cambio de estado |
| `seller_products` | Lista de productos de la papelería (activos/eliminados) |
| `product_form?productId={id}` | Agregar o editar un producto (sin `productId` = nuevo) |

## 2. Colecciones en Firestore

### `users`
`uid`, `firstName`, `lastName`, `email`, `phone`, `role` (`SELLER` | `CUSTOMER`), `hasStore`, `storeId`, `createAt`

### `stores`
`id`, `sellerId`, `name`, `description`, `category`, `imageURL`, `isActive`, `createAt`

### `products`
`id`, `storeId`, `name`, `description`, `category`, `price` (número), `stock` (número), `imageURL`, `isActive` (booleano), `createAt`

### `orders`
`id`, `customerId`, `customerName`, `customerEmail`, `items` (lista con `productId`, `name`, `price`, `quantity`), `total`, `status` (`PENDIENTE` | `EN_PREPARACION` | `ENTREGADO` | `CANCELADO`), `createAt`

---

## 3. Conectar el proyecto de Firebase `multistoreapp-a9203`

> ✅ **Listo:** `app/google-services.json` ya corresponde al proyecto `multistoreapp-a9203`
> y contiene el cliente `com.example.papeleriaapp`. Los pasos de abajo quedan como
> referencia por si hay que regenerar el archivo en el futuro.

1. Entra a la consola: <https://console.firebase.google.com/project/multistoreapp-a9203/overview>
2. En el menú lateral izquierdo, haz clic en el **icono de engranaje** (⚙️, esquina inferior izquierda) → **Configuración del proyecto**.
3. Pestaña **Tus apps** → botón **añadir app** → icono **Android** (el robot verde).
4. En **Nombre de paquete del paquete** escribe exactamente:
   ```
   com.example.papeleriaapp
   ```
   > Ojo: debe coincidir al carácter con el `applicationId` de `app/build.gradle.kts`, si no, el build falla.
5. Deja en blanco el campo de "Nombre del app" si quieres y pulsa **Registrar app**.
6. Pulsa **Descargar google-services.json**.
7. Reemplaza el archivo `app/google-services.json` de este proyecto por el recién descargado.
8. Vuelve a compilar (`gradlew assembleDebug`).

### 3.1 Activar Autenticación
1. Consola Firebase → **Build** → **Authentication** → **Empezar**.
2. Pestaña **Método de inicio de sesión** → activa **Correo/contraseña** → **Guardar**.

### 3.2 Crear la base de datos de Firestore
1. Consola Firebase → **Build** → **Firestore Database** → **Crear base de datos**.
2. Elige **Modo de producción** y la región más cercana → **Crear**.

### 3.3 Reglas de seguridad (para pruebas)
Pestaña **Reglas** y pega:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{userId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null && request.auth.uid == userId;
    }

    match /stores/{storeId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }

    match /products/{productId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }

    match /orders/{orderId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.customerId;
      allow update, read: if request.auth != null;
    }
  }
}
```

> Estas reglas son para desarrollo/práctica. En producción hay que endurecerlas.

---

## 4. Productos: dos formas de cargarlos

### 4.1 Desde la app (CRUD del vendedor) ✅
1. Inicia sesión como **vendedor** → en la pantalla de pedidos toca el icono de **caja de productos** (arriba a la derecha).
2. Botón **+** → completa nombre, descripción, categoría, precio y stock → **Agregar producto**.
3. En cada producto puedes **editar** (lápiz) o **eliminar** (papelera).
   - La eliminación es *baja lógica*: el producto desaparece del catálogo pero el documento se conserva
     para no romper los pedidos que ya lo incluían.
   - Con **"Mostrar eliminados"** puedes verlos y **restaurarlos** (icono de restaurar).

### 4.2 Desde la consola de Firestore (opcional, semilla)

1. Consola Firebase → **Firestore Database** → **Crear colección** → id: `products`.
2. Agrega documentos con estos campos (tipos exactos):

| Campo | Tipo | Ejemplo |
|---|---|---|
| `storeId` | string | id del documento de la colección `stores` (vacío `""` si aún no existe) |
| `name` | string | Bolígrafo azul Pilot |
| `description` | string | Bolígrafo de tinta gel, punta 0.7 mm |
| `category` | string | Escritura |
| `price` | number | 25.5 |
| `stock` | number | 50 |
| `imageURL` | string | `""` |
| `isActive` | boolean | `true` |
| `createAt` | number | 1730000000000 |

Ejemplos para armar el catálogo:

| name | category | price | stock |
|---|---|---|---|
| Bolígrafo azul Pilot | Escritura | 25.50 | 100 |
| Lápiz #2 Staedtler | Escritura | 12.00 | 150 |
| Cuaderno profesional 100 hojas | Cuadernos y libretas | 85.00 | 60 |
| Set de colores 24 piezas | Arte y manualidades | 120.00 | 30 |
| Resaltadores pastel x6 | Oficina | 65.00 | 45 |
| Goma y sacapuntas | Útiles escolares | 15.00 | 200 |

> Los productos deben tener `isActive = true` para aparecer en el catálogo.
> Las categorías deben coincidir con las de `StationeryCategories.productCategories` para que el filtro funcione.
> En `price` usa siempre decimales (ej. `25.5` en vez de `25`) para que Firestore lo guarde como número decimal.

---

## 5. Compilar y probar

```powershell
cd "C:\Users\Admin\Downloads\Nueva carpeta\PapeleriaApp"
.\gradlew.bat assembleDebug
```

Flujo de prueba:
1. Abrir la app → **¿No tienes cuenta? Regístrate** → elegir **Vendedor** → completar datos → registrar la papelería → pantalla de pedidos.
2. Como vendedor: icono de productos → **+** → crear un producto (ej. "Bolígrafo azul", categoría *Escritura*, precio 25.5, stock 100).
3. Cerrar sesión → registrar una cuenta de **Cliente** → ver el catálogo con ese producto.
4. Cliente: **Agregar** al carrito → icono del carrito → **Realizar pedido** → verlo en **Mis pedidos**.
5. Cerrar sesión → entrar como **vendedor** → el pedido aparece en "Pedidos de la papelería" y se le puede cambiar el estado.
6. Comprobar en la consola de Firestore que el **stock del producto bajó** y que existen los documentos en `users`, `stores`, `products` y `orders`.
