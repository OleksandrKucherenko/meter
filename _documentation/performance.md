# Typical Android Performance Questions

This is a FAQ for performance optimizers in Android projects.

## How fast is my layouts?

Q: My project contains multiple layouts. How to diagnose which one is the slowest? How to optimize?

A: First of all we need to measure the layouts inflate time. This is API level call which in most cases cannot be optimized
but influence a lot on application UX.

```java
public class LayoutsPerformanceTests extends PerformanceTests {
    /** Array of extracted fields. */
    private final SparseArray<String> mFields = new SparseArray<String>();

    private final static WeakHashMap<Context, Boolean> initialized = new WeakHashMap<Context, Boolean>();

    /** {@inheritDoc} */
    @Override
    protected void warmUp() {
        final Field[] fields = R.layout.class.getFields();

        for (Field f : fields) {
            final String name = f.getName();

            // exclude resources of 3rd party libs: Sherlock, Facebook, Support Lib
            final int index1 = name.indexOf("abs__");
            final int index2 = name.indexOf("com_facebook_");
            final int index3 = name.indexOf("abc_");

            if (-1 == index1 && -1 == index2 && -1 == index3) {
                try {
                    mFields.put(f.getInt(null), name);
                } catch (final Throwable ignored) {

                }
            }
        }
    }
    @SmallTest
    public void test_00_layouts_all() {
        for (int i = 0, len = mFields.size(); i < len; i++) {
            final int key = mFields.keyAt(i);
            final String value = mFields.valueAt(i);

            try {
                final View v = LayoutInflater.from(getContext()).inflate(key, null);
                Meter.beat("layout: " + value + " / views: " + totalViewsCount(v));
            } catch (final Throwable ignored) {
                // exclude <merge /> layouts
                Meter.skip("layout: " + value);
            }
        }
    }
	
	  /* [ UTILITY METHODS ] ========================================================================== */

    public static int totalViewsCount(final View v) {
        int childs = 1 /*count ourself*/;

        if (v instanceof ViewGroup) {
            final int len = ((ViewGroup) v).getChildCount();

            for (int i = 0; i < len; i++) {
                View nextChild = ((ViewGroup) v).getChildAt(i);

                childs += totalViewsCount(nextChild);
            }
        }

        return childs;
    }
}
```

_Notes: `totalViewsCount()` method influence on benchmarking. The best approach will be create warm-up code that will
inflate all views and will execute method on them. Than will store extracted values for tests._

Tactics/Solutions:
* try to use custom inflator, that increase speed of inflating from 5% to 50%, depends on how many custom views used in layouts.

## Which JSON library to choose?
  
Q: on market available at least 4 well known libraries that offer a great flexibility and speed. Which one is the 
fastest? Which one to choose? 

A: Lets test those libs: 
* `org.json' - Android original JSON package, 
* `com.alibaba.fastjson` - fast JSON from alibaba,
* `org.json.simple` - simple JSON,
* `net.minidev.json` - smart JSON,
* `com.google.gson` - GSON, Google JSON lib

You will need a dump of all JSON responses that your server can produce. Store them as a separated files in Unit 
Test `assets` folder. We will execute benchmarking on all those JSON responses and will see which one is the best.

```java
public class JsonVsXmlTests extends PerformanceTests {
    private String mJson;

    /** List of test JSON files. */
    private static final String[] TestFiles = new String[]{
            "test_02_callerid_store.json",
            "test_03_unblock.json",
            "test_05_connections.json",
            "test_07_country_list.json",
            "test_09_enhanced_status.json",
            "test_10_gscm.json",
            "test_15_profile.json",
            "test_16_profile_edit.json",
            "test_18_register.json",
            "test_19_search_by_number.json",
            "test_22_view_log.json",
            "test_xx_people_u_may_know.json",
    };

    /** Pre-loaded JSON files. */
    private final Map<String, String> mJsons = new HashMap<String, String>();

    /** {@inheritDoc} */
    @Override
    protected void warmUp() {
        InputStream isJson, isXml;

        try {
            final AssetManager assets = getUnitTestsContext().getAssets();

            if (mJsons.isEmpty()) {
                for (int i = 0, len = TestFiles.length; i < len; i++) {
                    final String fileName = TestFiles[i];
                    final InputStream is = assets.open(fileName);
                    final String json = FileUtil.toString(is);
                    is.close();

                    mJsons.put(fileName, json);
                }
            }
        } catch (final Throwable ignored) {
          // do nothing
        }

        Log.d(TAG, "Json's size: " + mJsons.length() );
    }
    
    /* [ JSON libs TESTS ] ===================================================================================== */

    @MediumTest
    public void test_00_parse_FastJson_requests() throws IOException {
        Meter.loop("Parse all known requests");
        for (Entry<String, String> entry : Jsons.entrySet()) {
            final String json = entry.getValue();

            com.alibaba.fastjson.JSONObject result = com.alibaba.fastjson.JSONObject.parseObject(json);
            Meter.recap();
        }
        Meter.unloop("Parsing done.");
    }

    @MediumTest
    public void test_01_parse_SimpleJson_requests() throws IOException {
        Meter.loop("Parse all known requests");
        for (Entry<String, String> entry : Jsons.entrySet()) {
            final String json = entry.getValue();

            org.json.simple.JSONObject result = (org.json.simple.JSONObject) org.json.simple.JSONValue.parse(json);
            Meter.recap();
        }
        Meter.unloop("Parsing done.");
    }

    @MediumTest
    public void test_02_parse_SmartJson_requests() throws IOException {
        Meter.loop("Parse all known requests");
        for (Entry<String, String> entry : Jsons.entrySet()) {
            final String json = entry.getValue();

            net.minidev.json.JSONObject result = (net.minidev.json.JSONObject) net.minidev.json.JSONValue.parse(json);
            Meter.recap();
        }
        Meter.unloop("Parsing done.");
    }
    
    @MediumTest
    public void test_03_parse_Gson_requests() throws IOException {
        final com.google.gson.JsonParser parser = new com.google.gson.JsonParser();

        Meter.loop("Parse all known requests");
        for (Entry<String, String> entry : Jsons.entrySet()) {
            final String json = entry.getValue();

            final com.google.gson.JsonElement result = parser.parse(json);
            Meter.recap();
        }
        Meter.unloop("Parsing done.");
    }    
}
```

Solution: compare results of all the tests. Customize logic of every test if needed - approaches in every project 
are different. Mostly each library has own set of performance hints. Try to make code small and clear.

_FYI: Smart JSON lib shows the best results on Android._