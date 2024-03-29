package khudiakov.kirill.goods.data

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import khudiakov.kirill.goods.data.network.ProductsApi

const val PRODUCT_ID_KEY = "product_id_key"

/**
 * Enumeration that represents current status of products loading.
 */
enum class ProductsApiStatus { LOADING, ERROR, DONE }

/**
 * Repository class that encapsulates receiving product list from the server.
 */
object ProductsRepository {

    private val _products = MutableLiveData<List<Product>>()

    /**
     * List of products received from remote source.
     */
    val products: LiveData<List<Product>>
        get() = _products

    private val _status = MutableLiveData<ProductsApiStatus>()

    /**
     * Current status of products loading.
     */
    val apiStatus: LiveData<ProductsApiStatus>
        get() = _status

    /**
     * Update current product list via retrofit service.
     */
    @SuppressLint("CheckResult")
    fun updateProducts() {
        val single = ProductsApi.retrofitService.getProducts()
        _status.value = ProductsApiStatus.LOADING

        // subscribe method returns Disposable. We don't create memory leaks,
        // so we can just ignore it and use Suppress annotation.
        single
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                _products.value = list
                _status.value = ProductsApiStatus.DONE
            }, {
                if (products.value == null) {
                    _status.value = ProductsApiStatus.ERROR
                } else {
                    _status.value = ProductsApiStatus.DONE
                }
            })
    }

    /**
     * Get product from repository with given id.
     * @param id Id of the product.
     * @return related product.
     */
    operator fun get(id: Long): Product? {
        return _products.value?.find { product ->
            product.id == id
        }
    }
}