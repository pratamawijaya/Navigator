package com.github.florent37.navigator

import android.app.Activity
import androidx.fragment.app.Fragment
import com.github.florent37.navigator.exceptions.MissingRequiredParameter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.Serializable
import kotlin.reflect.KProperty

open class Param : Serializable

/**
 * Retrieve a non nullable route Parameter
 *
 * val args = routeParamValue<MyRoute.MyParam>
 */
fun <T> Activity.routeParamValue(parameterClazz: Class<T>): T {
    return when {
        intent.hasExtra(ROUTE_FLAVOR_ARGS_KEY) -> {
            this.intent.getSerializableExtra(ROUTE_ARGS_KEY) as T
        }
        intent.hasExtra(ROUTE_KEY_STR_PARAMS) -> {
            val jsonString = intent.getStringExtra(ROUTE_KEY_STR_PARAMS)
            jsonString.fromJson(parameterClazz)
        }
        else -> {
            throw MissingRequiredParameter(parameterClazz.simpleName)
        }
    }
}

/**
 * Retrieve a non nullable flavor Parameter
 *
 * val args = flavorParamValue<MyRoute.MyParam>
 */
fun <T> Activity.flavorParamValue(parameterClazz: Class<T>): T {
    return when {
        intent.hasExtra(ROUTE_FLAVOR_ARGS_KEY) -> {
            this.intent.getSerializableExtra(ROUTE_FLAVOR_ARGS_KEY) as T
        }
        intent.hasExtra(ROUTE_KEY_STR_PARAMS) -> {
            val jsonString = intent.getStringExtra(ROUTE_KEY_STR_PARAMS)
            jsonString.fromJson(parameterClazz)
        }
        else -> {
            throw MissingRequiredParameter(parameterClazz.simpleName)
        }
    }
}

internal var moshi : Moshi? = null

internal fun <T> String.fromJson(clazz: Class<T>) : T {
    if(moshi == null) {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    val jsonAdapter = moshi!!.adapter<T>(clazz)
    return jsonAdapter.fromJson(this) as T
}

/**
 * Retrieve a nullable route Parameter
 *
 * val args  : MyRoute.MyParam? = optionalRouteParamValue<MyRoute.MyParam>
 */
fun <T> Activity.optionalRouteParamValue(parameterClazz: Class<T>): T? {
    return when {
        intent.hasExtra(ROUTE_ARGS_KEY) -> {
            this.intent?.getSerializableExtra(ROUTE_ARGS_KEY) as? T
        }
        intent.hasExtra(ROUTE_KEY_STR_PARAMS) -> {
            val jsonString = intent.getStringExtra(ROUTE_KEY_STR_PARAMS)
            jsonString.fromJson(parameterClazz)
        }
        else -> {
            throw MissingRequiredParameter(parameterClazz.simpleName)
        }
    }
}

/**
 * Retrieve a nullable flavor Parameter
 *
 * val args : MyRoute.MyParam? = optionalFlavorParamValue<MyRoute.MyParam>
 */
fun <T> Activity.optionalFlavorParamValue(parameterClazz: Class<T>): T? {
    return when {
        intent.hasExtra(ROUTE_FLAVOR_ARGS_KEY) -> {
            this.intent?.getSerializableExtra(ROUTE_FLAVOR_ARGS_KEY) as? T
        }
        intent.hasExtra(ROUTE_KEY_STR_PARAMS) -> {
            val jsonString = intent.getStringExtra(ROUTE_KEY_STR_PARAMS)
            jsonString.fromJson(parameterClazz)
        }
        else -> {
            throw MissingRequiredParameter(parameterClazz.simpleName)
        }
    }
}

class ParameterDelegate<T : Param>(val parameterClazz: Class<T>, val flavor: Boolean = false) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T {
        // return value
        if (thisRef != null && thisRef is Activity) {
            return if (flavor) {
                thisRef.flavorParamValue(parameterClazz)
            } else {
                thisRef.routeParamValue(parameterClazz)
            }
        } else if (thisRef != null && thisRef is Fragment && thisRef.activity != null) {
            return if (flavor) {
                thisRef.activity!!.flavorParamValue(parameterClazz)
            } else {
                thisRef.activity!!.routeParamValue(parameterClazz)
            }
        }
        throw MissingRequiredParameter(parameterClazz.simpleName)
    }

    operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>, value: T
    ) {
        //not implemented
    }
}

class OptionalParameterDelegate<T : Param>(
    val parameterClazz: Class<T>,
    val flavor: Boolean = false
) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T? {
        try {
            // return value
            return if (thisRef != null && thisRef is Activity) {
                if (flavor) {
                    thisRef.optionalFlavorParamValue<T>(parameterClazz)
                } else {
                    thisRef.optionalRouteParamValue<T>(parameterClazz)
                }
            } else if (thisRef != null && thisRef is Fragment && thisRef.activity != null) {
                if (flavor) {
                    thisRef.activity?.optionalFlavorParamValue<T>(parameterClazz)
                } else {
                    thisRef.activity?.optionalRouteParamValue<T>(parameterClazz)
                }
            } else {
                null
            }
        } catch (t: Throwable) {
            return null
        }
    }

    operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>, value: T?
    ) {
        //not implemented
    }
}

/**
 * Retrieve a non nullable route Parameter
 *
 * val args : MyRoute.MyParam by parameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Activity.parameter() =
    ParameterDelegate(parameterClazz = T::class.java)

/**
 * Retrieve a nullable route Parameter
 *
 * val args : MyRoute.MyParam? by parameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Activity.optionalParameter() =
    OptionalParameterDelegate(parameterClazz = T::class.java)

/**
 * Retrieve a non nullable route Parameter
 *
 * val args : MyRoute.MyParam by parameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Fragment.routeParameter() =
    ParameterDelegate(parameterClazz = T::class.java, flavor = false)

/**
 * Retrieve a nullable route Parameter
 *
 * val args : MyRoute.MyParam? by parameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Fragment.optionalRouteParameter() =
    OptionalParameterDelegate(parameterClazz = T::class.java, flavor = false)


/**
 * Retrieve a non nullable flavor Parameter
 *
 * val args : MyRoute.MyParam by flavorParameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Fragment.flavorParameter() =
    ParameterDelegate(parameterClazz = T::class.java, flavor = true)

/**
 * Retrieve a nullable flavor Parameter
 *
 * val args : MyRoute.MyParam by flavorParameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Fragment.optionalFlavorParameter() =
    OptionalParameterDelegate(parameterClazz = T::class.java, flavor = true)

/**
 * Retrieve a non nullable flavor Parameter
 *
 * val args : MyRoute.MyParam by flavorParameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Activity.flavorParameter() =
    ParameterDelegate(parameterClazz = T::class.java, flavor = true)

/**
 * Retrieve a nullable flavor Parameter
 *
 * val args : MyRoute.MyParam by flavorParameter<MyRoute.MyParam>()
 */
inline fun <reified T : Param> Activity.optionalFlavorParameter() =
    OptionalParameterDelegate(parameterClazz = T::class.java, flavor = true)


