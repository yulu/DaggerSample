#Dagger

From [Documentation by Google](http://google.github.io/dagger/)

##Dependency Injection
###WIKIPEDIA
In software engineering, **dependency injection** is a software design pattern that implements inversion of control for software libraries, where the caller delegates to an external framework the control flow of discovering and importing a service or software module. Dependency injection allows a program design to follow the dependency inversion principle where modules are loosely coupled. With dependency injection, the client part of a program which uses a module or service doesn't need to know all its details, and typically the module can be replaced by another one of similar characteristics without altering the client. An injection is the passing of a dependency (a service) to a dependent object (a client). The service is made part of the client's state. Passing the service to the client, rather than allowing a client to build or find the service, is the fundamental requirement of the pattern.


###EASY TO UNDERSTANDY BY JAMES SHORE
>Dependency injection means giving an object its instance variable. Really. That's it.

####The Slightly Longer Version, Part I: Dependency Non-Injection

Classes have these things they call methods on. Let's call those "dependencies". Most people call them "variables". Sometimes, when they're feeling fancy, they call them "instance variables".
    
    public class Example {
        private DatabaseThingie myDatabase;

        public Example() {
            myDatabase = new DatabaseThingie();
        }

        public void DoStuff() {
            . . .
            myDatabase.GetData();
            . . . 
        }
    }

Here, we have a variable.., uh, dependency.. named "myDatabase". We initialize it in the constructor.

####The Slightly Longer Version, Part II: Dependency Injeciton
If we wanted to, we could pass the variable into the constructor. That would "inject" the "dependency" into the class. Now when we use the variable (dependency), we use the object that we were given rather than the one we created.
    
    public class Example {
        private DatabaseThingie myDatabase;

        public Example() {
            myDatabase = new DatabaseThingie();
        }

        public Example(DatabaseThingie useThisDatabaseInstead) {
            myDatabase = useThisDatabaseInstead;
        }

        public void DoStuff() {
            . . .
            myDatabase.GetData();
            . . . 
        }
    }
            
That's really all there is to it. The rest is just variations on the theme. You could set the dependency in a setter method. You could set the dependency by calling a setter method that's defined in a special interface.

####The Slightly Longer Version, Part III: Why Do We Do This?
Among other things, it's handy for isolating classes during testing.
        
    public class ExampleTest {
        TestDoStuff() {
            MockDatabase mockDatabase = new MockDatabase();

            //MockDatabase is a subclass of DatabaseThingie, so we can
            //"inject" it here:
            Example example = new Example(mockDatabase);

            example.DoStuff();
            mockDatabase.AssertGetDataWasCalled();
        }
    }    

    public class Example {
        private DatabaseThingie myDatabase;

        public Example() {
            myDatabase = new DatabaseThingie();
        }

        public Example(DatabaseThingie useThisDatabaseInstead) {
            myDatabase = useThisDatabaseInstead;
        }

        public void DoStuff() {
            . . .
            myDatabase.GetData();
            . . . 
        }
    }

##Introduction To Dagger 2
The best classes in any application are the ones that do stuff: the `BarcodeDecoder`, the `KoopaPhysicsEngine`, and the `AudioStreamer`. These classes have dependencies; perhaps a `BarcodeCameraFinder`, `DefaultPhysicsEngine`, and an `HttpStreamer`.

To contrast, the worst classes in any application are the ones that take up space without doing much at all: the `BarcodeDecoderFactory`, the `CameraServiceLoader`, and the `MutableContextWrapper`. These classes are the clumsy duct tape that wires the interesting stuff together.

Dagger is a replacement for these `FactoryFactory` classes. It allows you to focus on the interesting classes. Declare dependencies, specify how to satisfy them, and ship your app.

By building on standard `javax.inject` annotation (JSR-330), each class is easy to test. You don't need a bunch of boilerplate just to swap the `RpcCreditCardService` out of a `FakeCreditCardService`.

Dependency injection isn't just for testing. It also makes it easy to create reusable, interchangeable modules. You can share the same `AuthenticationModule` across all of your apps. And you can run `DevLogginModule` during development and `ProdLoggingModule` in production to get the right behavior in each situation.

Dependency injection frameworks have existed for years with a whole variety of APIs for configuring and injecting. So, why reinvent the wheel? Dagger 2 is the first to **implement the full stack with generated code**. The guiding principle is to generate code that mimics the code that a user might have hard-written to ensure that dependency injection is a simple, traceable and performant as it can be. 

##Using Dagger
We'll demonstrate dependency injection and Dagger by building a coffee maker. 

###DECLARE DEPENENCIES
Dagger constructs instances of your application classes and satisfies their dependencies. It uses the `javax.inject.Inject` annotation to identify which constructors and fields it is interested in.

Use `@Inject` to annotate the constructor that Dagger should use to create instances of a class. When a new instance is requested, Dagger will obtain the required parameters values and invoke this constructor.


    class Thermosiphon implements Pump {
        private final Heater heater;
        
        @Inject
        Thermosiphon(Heater heater) {
            this.heater = heater;
        }
    
        . . .
    }


Dagger can inject fields directly. In this example it obtains a `Heater` instance for the `heater` field and a `Pump` instance for the `pump` fields.


    class CoffeeMaker {
        @Inject Heater heater;
        @Inject Pump pump;    
        
        . . .
    }

If your class has `@Inject`-annotated fields but no `@Inject`-annotated constructor, Dagger will inject those fields if requested, but will not create new instances. Add a no-argument constructor with the `@Inject` annotation to indicate that Dagger may create instances as well.

Dagger also supports method injection, though constructor or field injection are typically preferred.

Classes that lack `@Inject` annotations cannot be constructed by Dagger.

###SATISFYING DEPENDENCIES
By default, Dagger satisfies each dependency by constructing an instance of the requested type as described above. When you request a `CoffeeMaker`, it'll obtain one by calling `new CoffeeMaker()` and setting its injectable fields.

But `@Inject` doesn't work everywhere:

- Interface can't be constructed.
- Third-party classes can't be annotated.
- Configurable objects must be configured!


For these cases where `@Inject` is insufficient or awkward, use an `@Provides`-annotated method to satisfy a dependency. The method's return type defines which dependency it satisfies.


For example, `provideHeater()` is invoked whenever a `Heater` is required:

    
    @Provides Heater provideHeater() {
        return new ElectricHeater();
    }    


It's possible for `@Provides` methods to have dependencies of their own. This one returns a `Thermosiphon` whenever a `Pump` is required:


    @Provides Pump providePump(Thermosiphon pump) {
        return pump;
    }
    
All `@Provides` methods must belong to a module. These are just classes that have an `@Module` annotation.

    
    @Module
    class DripCoffeeModule {
        @Provides Heater provideHeater() {
            return new ElectricHeater();
        }
        
        @Provides Pump providePump(Thermosiphon pump) {
            return pump;
        }
    }    
    
    
By convention, `@Provides` methods are named with a `provide` prefix and module classes are named with a `Module` suffix.


###BUILDING THE GRAPH
The `@Inject` and `@Provides`-annotated classes form a graph of objects, linked by their dependencies. Calling code like an application's main method or an Android `Application` accesses that graph via a well-defined set of roots. In Dagger 2, that set is defined by an interface with methods that have no arguments and return the desired type. By applying the `@Component` annotation to such an interface and passing the `module` types to the `module` parameter, Dagger 2 then fully generates an implementation of that contract.

    @Component(modules = DripCoffeeModule.class)
    interface CoffeeShop {
        CoffeeMaker maker();
    }

The implementation has the same name as the interface prefixed with `Dagger_`. Obtain an instance by invoking the `builder()` method on that implementation and use the returned `builder` to set dependencies and `build()` a new instance.

    CoffeeShop coffeeShop = Dagger_CoffeeShop.builder()
            .dripCoffeeModule(new DripCoffeeModule())
            .build();

Any module with an accessible default constructor can be elided as the builder will construct an instance automatically if none is set. If all dependencies can be constructed in that manner, the generated implementation will also have a `create()` method that can be used to get a new instance without having to deal with the builder.

    CoffeeShop coffeeShop = Dagger_CoffeeShop.create();

Now, our `CoffeeApp` can simply use the Dagger-generated implementation of `CoffeeShop` to get a fully-injected `CoffeeMaker`.

    public class CoffeeApp {
        public static void main(String[] args) {
            CoffeeShop coffeeShop = Dagger_CoffeeShop.create();
            coffeeShop.maker().brew();
        }
    }

Now that the graph is constructed and the root object is injected, we can run our coffee maker app. 

###SINGLETONS AND SCOPED BINDINGS
Annotate an `@Provides` method or injectable class with `@Singleton`. The graph will use a single instance of the value for all of its clients.

    @Provides @Singleton Heater provideHeater() {
        return new ElectricHeater();
    }

The `@Singleton` annotation on an injectable class also serves as documentation. It reminds potential maintainers that this class may be shared by multiple threads.

    @Singleton
    class CoffeeMaker {
        . . .    
    }

Since Dagger 2 associates scoped instances in the graph with instances of component implementations, the components themselves need to declare which scope they intend to represent. For example, it wouldn't make any sense to have `@Singleton` binding and a `@RequestScoped` binding in the same component because those scopes have different lifecycle and thus must live in components with different lifecycles. Declaring that a component is associated with a given scope, simply apply the scope annotation to the component interface.

    @Component(module = DripCoffeeModule.class)
    @Singleton
    interface CoffeeShop {
        CoffeeMaker maker();
    }

###LAZY INJECTIONS
Sometimes you need an object to be instantiated lazily. For any binding `T`, you can create a `Lazy<T>` which defers instantiation until the first call to `Lazy<T>`'s `get()` method. If `T` is a singleton, then `Lazy<T>` will be the same instance for all injections within the `ObjectGraph`. Otherwise, each injection site will get its own `Lazy<T>` instance. Regardless, subsequent calls to any given instance of `Lazy<T>` will return the same underlying instance `T`.

    class GridingCoffeeMaker {
        @Inject Lazy<Grinder> lazyGrinder;

        public void brew() {
            while (needsGrinding()) {
                //Grinder create once on first call to .get() and cached.
                lazyGrinder.get().grind();
            }
        }
    }

###PROVIDER INJECTIONS
Sometimes you need multiple instance to be returned instead of just injection a single value. While you have several options (Factories, Builders, etc.), one option is to inject a `Provider<T>` instead of just `T`. A `Provider<T>` invokes the *binding logic* for `T` each time `.get()` is called. If that binding logic is an `@Inject` constructor, a new instance will be created, but a `@Provides` method has no such guarantee.

    class BigCoffeeMaker {
        @Inject Provider<Filter> filterProvider;

        public void brew(int numberOfPots) {
            . . .
            for (int p = 0; p < numberOfPots; p++) {
                maker.addFilter(filterProvider.get()); //new filter every time
                maker.addCoffee(…);
                maker.percolate();
                . . .
            }
        }
    }

**Note**: Injecting `Provider<T>` has the possibility of creating confusing code, and may be a design smell of mis-scoped or mis-structured objects in your graph. Often you will want to use factory or a `Lazy<T>` or re-organize the lifetimes and structure of your code to be able to just inject a `T`. Injecting `Provider<T>` can, however, be a life saver in some cases. A common use is when you must use a legacy architecture that doesn't line up with your object's natural lifetimes.

###QUALIFIERS
Sometimes the type alone is insufficient to identify a dependency. For example, a sophisticated coffee maker app may want separate heaters for the water and the hot plate.

In this case, we add a **qualifier annotation**. This is any annotation that itself has a `@Qualifier` annotation. Here's the declaration of `@Named`, a qualifier annotation included in `javax.inject`:

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    public @interface Named {
        String value() default "";
    }

You can create your own qualifier annotations, or just use @Named. Apply qualifiers by annotating the field or parameter of interest. The type and qualifier annotation will both be used to identify the dependency.

    class ExpensiveCoffeeMaker {
        @Inject @Named("water") Heater waterHeater;
        @Inject @Named("hot plate") Heater hotPlateHeater;
        . . .
    }

Supply qualified values by annotating the corresponding @Provides method.

    @Provides @Named("hot plate") Heater provideHotPlateHeater() {
        return new ElectricHeater(70);
    }

    @Provides @Named("water") Heater provideWaterHeater() {
        return new ElectricHeater(93);
    }

Dependencies may not have multiple qualifier annotations.

###COMPILE-TIME VALIDATION
The Dagger annotation processor is strict and will cause a compiler error if any bindings are invalid or incomplete. For example, this module is installed in a component, which is missing a binding for `Executor`:

    @Module
    class DripCoffeeModule {
        @Provides Heater provideHeater(Executor executor) {
            return new CpuHeater(executor);
        }
    }

When compiling it, `javac` rejects the missing binding:

    [ERROR] COMPILATION ERROR:
    [ERROR] error: java.util.concurrent.Executor cannot be provided without an @Provides-annotated method.

Fix the problem by adding an `@Provides`-annotated method for `Executor` to *any* of the modules in the component. While `@Inject`, `@Module` and `@Provides` annotations are validated individually, all validation of the relationship between bindings happens at the `@Component` level. Dagger 1 relied strictly on `@Module`-level validation (which may or may not have reflected runtime behavior), but Dagger 2 elides such validation (and the accompanying configuration parameters on `@Module`) in favor of full graph validation.
###COMPILE-TIME CODE GENERATION
Dagger's annotation processor may also generate source files with names like `CoffeeMaker$$Factory.java` or `CoffeeMaker$$MembersInjector.java`. These files are Dagger implementation details. You shouldn't need to use them directly, though they can be handy when step-debugging through an injection.

##Using Dagger in Your Build
You will need to include the `dagger-2.0-SNAPSHOT.jar` in your application's runtime. In order to activate code generation you will need to include `dagger-compiler-2.0-SNAPSHOT.jar` in your build at compile time. In gradle, add the dependencies:

    dependencies {
        compile 'com.google.dagger:dagger:2.0-SNAPSHOT'
        apt 'com.google.dagger:dagger-compiler:2.0-SNAPSHOT'
        provided 'org.glassfish:javax.annotation:10.0-b28'
    }    



        

    


