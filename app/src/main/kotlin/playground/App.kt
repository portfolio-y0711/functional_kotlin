/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package playground

interface CreditCard

data class Charge(
    val card: CreditCard,
    val productId: String,
    val isPaid: Boolean
)

data class ProductDetail(
    val category: String,
    val name: String,
    val price: Double,
) {
    companion object {
        fun of(
            price: Double,
            name: String,
            category: String,
        ): ProductDetail {
            return ProductDetail(category = category, name = name, price = price)
        }
    }
}

data class MenuElement(
    val uuid: String,
    val menuDetail: ProductDetail
)

interface Product {
    var productId: String
    var detail: ProductDetail
    val factory: (String, ProductDetail) -> Product
}

val defaultMenuDetail = ProductDetail(category = "", name = "", price = 0.0)

class Beverage() : Product {
    override var productId: String = ""
    override var detail: ProductDetail = defaultMenuDetail

    constructor(productId: String, menuDetail: ProductDetail) : this() {
        this.detail = menuDetail
        this.productId = productId
    }

    override val factory: (
        productId: String,
        productDetail: ProductDetail,
    ) -> Beverage
        get() = { productId, productDetail -> Beverage(productId, productDetail) }

    override fun toString(): String {
        return "name:${this.detail.name},price:${this.detail.price}"
    }
}

class Dessert() : Product {
    override var productId: String = ""
    override var detail: ProductDetail = defaultMenuDetail

    constructor(productId: String, productDetail: ProductDetail) : this() {
        this.detail = productDetail
        this.productId = productId
    }

    override val factory: (
        productId: String,
        productDetail: ProductDetail,
    ) -> Dessert
        get() = { productId, productDetail -> Dessert(productId, productDetail) }

    override fun toString(): String {
        return "name:${this.detail.name},price:${this.detail.price}"
    }
}

class Cafe {
    @PublishedApi
    internal val factory: HashMap<String, Pair<(String, ProductDetail) -> Product, ProductDetail>> = hashMapOf()

    @PublishedApi
    internal val productByCategory: HashMap<String, Product> = hashMapOf(
        "coffee" to Beverage(),
        "cake" to Dessert(),
    )

    fun addFactory(vararg products: Pair<String, ProductDetail>) {
        products.forEach { (uuid, productDetail) ->
            val product = productByCategory[productDetail.category]!!
            factory[uuid] = product.factory to productDetail
        }
    }

    override fun toString(): String {
        return this.factory.toList().map { (uuid, factory) ->
            uuid to factory
        }.toString()
    }

    fun getPriceById(uuid: String): Double {
        val (factory, productDetail) = factory[uuid]!!
        val product = factory(uuid, productDetail)
        return product.detail.price
    }

    fun getNameById(uuid: String): Any {
        val (factory, productDetail) = factory[uuid]!!
        val product = factory(uuid, productDetail)
        return product.detail.name
    }
}

data class ShoppingBasket(val cafe: Cafe) {
    var charges: List<Charge> = listOf()
    fun addProduct(
        card: CreditCard, productId: String
    ) {
        charges = charges.plus(
            Charge(
                card, productId, isPaid = false
            )
        )
    }

    fun settle() {
        charges = charges.map {
            Charge(
                card = it.card, productId = it.productId, isPaid = true
            )
        }
    }

    fun getReceipt(): List<Triple<Any, Double, Int>> {
        return charges.groupingBy {
            it.productId
        }.eachCount()
            .toList()
            .map { (uuid, qty) ->
                Triple(cafe.getNameById(uuid), cafe.getPriceById(uuid) * qty, qty)
            }
    }
}

interface MenuClient {
    fun getMenus(): List<MenuElement>
}

fun main() {
    val cafe: Cafe = Cafe().also { cafe ->
        val menus = object : MenuClient {
            override fun getMenus(): List<MenuElement> {
                return listOf(
                    MenuElement(
                        uuid = "cbf650ce-96ac-41f0-808a-ad6c73456cfd", menuDetail = ProductDetail.of(
                            category = "coffee", name = "americano", price = 1000.0,
                        )
                    ), MenuElement(
                        uuid = "fbc3e51d-816f-4174-8580-ac73e1e2d24e", menuDetail = ProductDetail.of(
                            category = "coffee", name = "caffe latte", price = 2000.0,
                        )
                    )
                )
            }
        }.getMenus()

        menus.forEach { it ->
            cafe.addFactory(it.uuid to it.menuDetail)
        }
    }

    val shoppingBasket = ShoppingBasket(cafe).also {
        it.addProduct(
            card = object : CreditCard {}, "cbf650ce-96ac-41f0-808a-ad6c73456cfd"
        )
        it.addProduct(
            card = object : CreditCard {}, "cbf650ce-96ac-41f0-808a-ad6c73456cfd"
        )
        it.addProduct(
            card = object : CreditCard {}, "cbf650ce-96ac-41f0-808a-ad6c73456cfd"
        )
        it.addProduct(
            card = object : CreditCard {}, "fbc3e51d-816f-4174-8580-ac73e1e2d24e"
        )
    }

    shoppingBasket.settle()
    val receipt = shoppingBasket.getReceipt()
    println(receipt)

}

