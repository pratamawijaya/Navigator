package com.github.florent37.navigator.starter

import android.content.Intent
import android.os.Bundle
import com.github.florent37.navigator.*
import com.github.florent37.navigator.exceptions.MissingIntentThrowable
import com.github.florent37.navigator.exceptions.PathNotFound
import org.json.JSONObject

typealias IntentConfig = (Intent) -> Unit

class NavigatorStarter(
    private val starterHandler: StarterHandler,
    private val routeListener: RouteListener,
    private val routing: Map<Destination, Routing>
) {
    fun <T : Route> push(route: T, intentConfig: IntentConfig? = null): Boolean {
        val success =
            this.startInternal(destination = route, resultCode = null, intentConfig = intentConfig)
        if (success) {
            routeListener.push(route)
        }
        return success
    }

    fun <T : Route> pushReplacement(route: T, intentConfig: IntentConfig? = null): Boolean {
        val success = this.startInternal(
            destination = route,
            resultCode = null,
            intentConfig = intentConfig,
            finishActivity = true
        )

        if (success) {
            routeListener.pushReplacement(route)
        }
        return success
    }

    fun pop(resultCode: Int, data: Intent?) {
        starterHandler.activity?.let {
            it.setResult(resultCode, data)
            it.finish()
        }
    }

    fun <P : Parameter, T : RouteWithParams<P>> push(
        route: T,
        arguments: P,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            destination = route,
            resultCode = null,
            intentConfig = intentConfig,
            routeParameter = arguments
        )
        if (success) {
            routeListener.push(route)
        }
        return success
    }

    fun <P : Parameter, T : RouteWithParams<P>> pushReplacement(
        route: T,
        arguments: P,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            destination = route,
            resultCode = null,
            intentConfig = intentConfig,
            routeParameter = arguments,
            finishActivity = true
        )
        if (success) {
            routeListener.pushReplacement(route)
        }
        return success
    }

    fun <T : Route> pushForResult(
        route: T,
        resultCode: Int,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            destination = route,
            resultCode = resultCode,
            intentConfig = intentConfig
        )
        if (success) {
            routeListener.push(route)
        }
        return success
    }

    fun <P : Parameter, T : RouteWithParams<P>> pushForResult(
        route: T,
        resultCode: Int,
        arguments: P,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            destination = route,
            resultCode = resultCode,
            intentConfig = intentConfig,
            routeParameter = arguments
        )
        if (success) {
            routeListener.push(route)
        }
        return success
    }

    /**
     * For route
     * if route has arguments => routeArguments != null
     */
    private fun <T : AbstractRoute> startInternal(
        destination: T,
        resultCode: Int? = null,
        intentConfig: IntentConfig? = null,
        routeParameter: Parameter? = null,
        finishActivity: Boolean = false
    ): Boolean {
        val containRoute = routing.containsKey(destination)
        val context = starterHandler.context ?: return false
        if (containRoute) {
            val intentCreator = routing[destination]
            intentCreator?.let {

                val intent: Intent = it.creator(context)

                val extras = Bundle()
                routeParameter?.let {
                    extras.putSerializable(ROUTE_ARGS_KEY, routeParameter)
                }

                extras.putString(ROUTE_INTENT_KEY, destination.path)

                intent.putExtras(extras)

                intentConfig?.invoke(intent)

                if (resultCode == null) {
                    starterHandler.start(intent)
                } else {
                    starterHandler.startForResult(intent, resultCode)
                }

                if (finishActivity) {
                    starterHandler.activity?.finish()
                }
            } ?: run {
                throw MissingIntentThrowable(routeName = destination.path)
            }
        } else {
            throw MissingIntentThrowable(routeName = destination.path)
        }
        return containRoute
    }

    fun <T : Flavor<*>> push(
        routeConfiguration: T,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            routeConfiguration = routeConfiguration,
            resultCode = null,
            intentConfig = intentConfig
        )
        if (success) {
            routeListener.push(routeConfiguration)
        }
        return success
    }

    fun <FR : Parameter, ROUTE : Route, F : FlavorWithParams<ROUTE, FR>> push(
        routeConfiguration: F,
        arguments: FR,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            routeConfiguration = routeConfiguration,
            resultCode = null,
            intentConfig = intentConfig,
            flavorParameters = arguments
        )
        if (success) {
            routeListener.push(routeConfiguration)
        }
        return success
    }

    fun <FR : Parameter, RP : Parameter, ROUTE : RouteWithParams<RP>, F : FlavorWithParams<ROUTE, FR>> push(
        routeConfiguration: F,
        routeArguments: RP,
        arguments: FR,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            routeConfiguration = routeConfiguration,
            resultCode = null,
            intentConfig = intentConfig,
            routeParameters = routeArguments,
            flavorParameters = arguments
        )
        if (success) {
            routeListener.push(routeConfiguration)
        }
        return success
    }

    fun <T : Flavor<*>> pushForResult(
        route: T,
        resultCode: Int,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            routeConfiguration = route,
            resultCode = resultCode,
            intentConfig = intentConfig
        )
        if (success) {
            routeListener.push(route)
        }
        return success
    }

    fun <R : Parameter, T : FlavorWithParams<*, R>> pushForResult(
        route: T,
        resultCode: Int,
        arguments: R,
        intentConfig: IntentConfig? = null
    ): Boolean {
        val success = this.startInternal(
            routeConfiguration = route,
            resultCode = resultCode,
            intentConfig = intentConfig,
            flavorParameters = arguments
        )
        if (success) {
            routeListener.push(route)
        }
        return success
    }

    /**
     * For flavors
     * if flavor has params => flavorParameters != null
     * if route has params => routeParameters != null
     */
    private fun <T : AbstractFlavor<*>> startInternal(
        routeConfiguration: T,
        resultCode: Int? = null,
        intentConfig: IntentConfig? = null,
        routeParameters: Parameter? = null,
        flavorParameters: Parameter? = null
    ): Boolean {

        val destination = routeConfiguration.route

        val containRoute = routing.containsKey(destination)
        val context = starterHandler.context ?: return false
        if (containRoute) {
            val intentCreator = routing[destination]
            intentCreator?.let {

                val extras = Bundle()

                extras.putString(ROUTE_INTENT_KEY, destination.path)
                extras.putString(SUB_ROUTE_INTENT_KEY, routeConfiguration.path)

                if (flavorParameters != null) {
                    extras.putSerializable(ROUTE_FLAVOR_ARGS_KEY, flavorParameters)
                }
                if (routeParameters != null) {
                    extras.putSerializable(ROUTE_ARGS_KEY, routeParameters)
                }

                val intent: Intent = it.creator(context)

                intent.putExtras(extras)

                intentConfig?.invoke(intent)

                if (resultCode == null) {
                    starterHandler.start(intent)
                } else {
                    starterHandler.startForResult(intent, resultCode)
                }
            } ?: run {
                throw MissingIntentThrowable(routeName = destination.path)
            }
        } else {
            throw MissingIntentThrowable(routeName = destination.path)
        }
        return containRoute
    }

    fun push(path: String) {
        return pushInternal(path = path)
    }

    fun pushForResult(path: String, resultCode: Int) {
        return pushInternal(path = path, resultCode = resultCode)
    }

    private fun pushInternal(path: String, resultCode: Int? = null) {
        //try to find by name
        Navigator.findDestination(path = path)?.let { destination ->
            when (destination) {
                is Route -> {
                    push(destination)
                    return
                }
                is Flavor<*> -> {
                    push(destination)
                    return
                }
                else -> { /* nothing to do */
                }
            }
        }
        //try to find by regex
        Navigator.findDestinationWithParams(path = path)?.let {
            pushWithParamsInternal(
                destinationWithParams = it,
                resultCode = resultCode
            )
            return
        }

        throw PathNotFound(path)
    }

    private fun pushWithParamsInternal(
        destinationWithParams: DesintationWithParams,
        resultCode: Int? = null
    ): Boolean {
        val context = starterHandler.context ?: return false
        val destination = destinationWithParams.destination

        val extras = Bundle()

        if (destination is AbstractFlavor<*>) {
            extras.putString(SUB_ROUTE_INTENT_KEY, destination.path)
            extras.putString(ROUTE_INTENT_KEY, destination.route.path)
        } else {
            extras.putString(ROUTE_INTENT_KEY, destination.path)
        }

        val json = JSONObject()
        destinationWithParams.params.forEach {
            json.put(it.key, it.value)
        }
        val jsonString = json.toString()

        extras.putString(ROUTE_KEY_STR_PARAMS, jsonString)

        destinationWithParams.routing.let {
            val intent: Intent = it.creator(context)

            intent.putExtras(extras)

            if (resultCode == null) {
                starterHandler.start(intent)
            } else {
                starterHandler.startForResult(intent, resultCode)
            }
        }

        return true
    }
}