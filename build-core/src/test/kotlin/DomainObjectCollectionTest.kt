import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Namer
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.DefaultDomainObjectCollection
import org.gradle.api.internal.DefaultNamedDomainObjectList
import org.gradle.api.internal.collections.ListElementSource
import org.gradle.api.specs.Spec
import org.gradle.internal.reflect.DirectInstantiator
import org.junit.Test
import org.junit.Assert.*


inline fun <reified T> getJavaClass(): Class<T> = T::class.java


class DomainObjectCollectionTest {


    @Test
    fun testCreateCollection() {


        val collection = DefaultNamedDomainObjectList(
            getJavaClass<String>(), DirectInstantiator.INSTANCE, {
                it
            }, CollectionCallbackActionDecorator.NOOP
        )
        collection.addAll(listOf("abc", "a", "c"))

        collection
            .matching { it.length == 1 }
            .forEach { println(it) }

        assertEquals(3, collection.size)
        println(collection.getByName("a"))
    }
}