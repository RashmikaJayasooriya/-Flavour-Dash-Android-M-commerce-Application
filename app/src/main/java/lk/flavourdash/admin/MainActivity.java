package lk.flavourdash.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
//import android.widget.SearchView;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import lk.flavourdash.admin.receiver.NetworkChangeReceiver;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private String[] suggestions = {"Home", "Manage Category", "Manage Sub Category", "Manage Dishes", "View Dishes", "View Customers", "Manage Orders", "Manage Branches", "View Branches"};
    private ArrayAdapter<String> suggestionAdapter;
    private androidx.cursoradapter.widget.CursorAdapter cursorAdapter;
    private SensorManager sensorManager;
    private long lastUpdate;
    private static final int SHAKE_THRESHOLD = 6000;
    private float last_x, last_y, last_z;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

//      Navigation
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        loadFragment(new HomeFragment());


//      Search
        SearchView searchView = findViewById(R.id.search_view);

        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);

        String[] from = new String[]{"text"};
        int[] to = new int[]{android.R.id.text1};
        cursorAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                null,
                new String[]{"text"},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        searchView.setSuggestionsAdapter(cursorAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSuggestions(newText);
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = cursorAdapter.getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
                    int columnIndex = cursor.getColumnIndex("text");

                    if (columnIndex != -1) {
                        String suggestion = cursor.getString(columnIndex);

                        handleSuggestionItemClick(suggestion);
                        Toast.makeText(MainActivity.this, "Clicked suggestion: " + suggestion, Toast.LENGTH_SHORT).show();

                        Fragment fragment = getFragmentForSuggestion(suggestion);
                        loadFragment(fragment);

                        selectDrawerItemForFragment(fragment);

                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        });

//        Broadcast receiver
        networkChangeReceiver = new NetworkChangeReceiver();

    }

    private Fragment getFragmentForSuggestion(String suggestion) {
        if ("Home".equalsIgnoreCase(suggestion)) {
            return new HomeFragment();
        } else if ("Manage Category".equalsIgnoreCase(suggestion)) {
            return new CategoryFragment();
        }else if ("Manage Sub Category".equalsIgnoreCase(suggestion)) {
            return new SubCategoryFragment();
        }else if ("Manage Dishes".equalsIgnoreCase(suggestion)) {
            return new DishAddFragment();
        }else if ("View Dishes".equalsIgnoreCase(suggestion)) {
            return new DishViewFragment();
        }else if ("View Customers".equalsIgnoreCase(suggestion)) {
            return new CustomersViewFragment();
        }else if ("Manage Orders".equalsIgnoreCase(suggestion)) {
            return new OrdersFragment();
        }else if ("Manage Branches".equalsIgnoreCase(suggestion)) {
            return new NewBranchSelectFragment();
        }else if ("View Branches".equalsIgnoreCase(suggestion)) {
            return new BranchViewFragment();
        }

        return null;
    }

    private void selectDrawerItemForFragment(Fragment fragment) {
        if (fragment != null) {
            MenuItem menuItem = null;

            if (fragment instanceof HomeFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.sideNavHome);
            } else if (fragment instanceof CategoryFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.addCategory);
            } else if (fragment instanceof SubCategoryFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.addSubCategory);
            } else if (fragment instanceof DishAddFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.addDish);
            } else if (fragment instanceof DishViewFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.viewDish);
            } else if (fragment instanceof CustomersViewFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.customerView);
            } else if (fragment instanceof OrdersFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.orderView);
            } else if (fragment instanceof NewBranchSelectFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.viewBranches);
            } else if (fragment instanceof BranchViewFragment) {
                menuItem = navigationView.getMenu().findItem(R.id.addBranches);
            }

            if (menuItem != null) {
                uncheckAllMenuItems(navigationView.getMenu());
                menuItem.setChecked(true);
                // Update the UI to reflect the change
                updateDrawerItemUI(menuItem);
            }
        }
    }

    private void updateDrawerItemUI(MenuItem menuItem) {
        View actionView = MenuItemCompat.getActionView(menuItem);
        if (actionView != null) {
            actionView.performClick();
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.sideNavHome) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.addCategory) {
            fragment = new CategoryFragment();
        } else if (itemId == R.id.addSubCategory) {
            fragment = new SubCategoryFragment();
        } else if (itemId == R.id.addDish) {
            fragment = new DishAddFragment();
        } else if (itemId == R.id.viewDish) {
            fragment = new DishViewFragment();
        } else if (itemId == R.id.customerView) {
            fragment = new CustomersViewFragment();
        } else if (itemId == R.id.orderView) {
            fragment = new OrdersFragment();
        } else if (itemId == R.id.viewBranches) {
            fragment = new NewBranchSelectFragment();
        } else if (itemId == R.id.addBranches) {
            fragment = new BranchViewFragment();
        } else if (itemId == R.id.sideNavLogout) {
            logOut();
        }
        loadFragment(fragment);
        uncheckAllMenuItems(navigationView.getMenu());
        item.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void uncheckAllMenuItems(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.hasSubMenu()) {
                uncheckAllMenuItems(menuItem.getSubMenu());
            }
            menuItem.setChecked(false);
        }
    }

    public void loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();
        }
    }

    //    Search
    private void filterSuggestions(String query) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "text"});
        for (int i = 0; i < suggestions.length; i++) {
            if (suggestions[i].toLowerCase().contains(query.toLowerCase())) {
                cursor.addRow(new Object[]{i, suggestions[i]});
            }
        }
        cursorAdapter.changeCursor(cursor);
    }

    private void handleSuggestionItemClick(String selectedItem) {
        // Perform fragment change based on the selected item
        Fragment fragment=null;
        switch (selectedItem) {
            case "Home":
                fragment = new HomeFragment();
                break;
            case "Product Add":
//                fragment = new ProductAddFragment();
                break;
            default:
                fragment = new HomeFragment();
                break;
        }

        loadFragment(fragment);
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent=new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastUpdate) > 100) {
                long diffTime = (currentTime - lastUpdate);
                lastUpdate = currentTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    recreate();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        unregisterReceiver(networkChangeReceiver);
    }
}
