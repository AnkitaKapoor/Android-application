package com.example.sengloke.InfoGo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.sengloke.InfoGo.ActivityRecognition.RecognitionFragment;
import com.example.sengloke.InfoGo.bluetooth.BluetoothActivity;
import com.example.sengloke.InfoGo.bluetooth.BluetoothFragment;
import com.example.sengloke.InfoGo.geofencing.GeofenceActivity;
import com.example.sengloke.InfoGo.geofencing.GeofenceFragment;
import com.example.sengloke.InfoGo.indoorLocation.IndoorActivity;
import com.example.sengloke.InfoGo.indoorLocation.IndoorFragment;
import com.example.sengloke.InfoGo.locationMarkers.ItemDetailMapActivity;
import com.example.sengloke.InfoGo.locationMarkers.ItemDetailMapFragment;
import com.example.sengloke.InfoGo.pictureTaking.MainActivity;
import com.example.sengloke.InfoGo.pictureTaking.MainFragment;
import com.example.sengloke.InfoGo.places.placesActivity;
import com.example.sengloke.InfoGo.places.placesFragment;
import com.example.sengloke.InfoGo.wifi.WifiActivity;
import com.example.sengloke.InfoGo.wifi.WifiFragment;
import com.google.firebase.auth.FirebaseAuth;




/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends Activity
        implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id)
    {
   //     FragmentManager fragmentManager = getSupportFragmentManager();

        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);

                if (id.equals("1")) {

                ItemDetailMapFragment fragmentWithMap = new ItemDetailMapFragment();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragmentWithMap)
                            .commit();


                }

                else if (id.equals("2")) { // for option item 2, use a map fragment


                    placesFragment fragment = new placesFragment();

                    getFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();

                }
                else if (id.equals("3")) { // for option item 2, use a map fragment


                    GeofenceFragment fragment = new GeofenceFragment();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                }


                else if (id.equals("4")) { // for option item 2, use a map fragment


                        WifiFragment fragment = new WifiFragment();

                          getFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                                .commit();

                }

                else if (id.equals("5")) { // for option item 2, use a map fragment


                        BluetoothFragment fragment = new BluetoothFragment();
                    // mapfragment.setArguments(arguments); // not used in this example
                    getFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();

                }

                else if (id.equals("7")) { // for option item 2, use a picture taking fragment(pictureTaking.MainFragment)


                    MainFragment fragment = new MainFragment();
                    // mapfragment.setArguments(arguments); // not used in this example
                    getFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();

                }

                else if (id.equals("8")) { // for option item 8, activity recognition fragment)

                    RecognitionFragment fragment = new RecognitionFragment();
                    // mapfragment.setArguments(arguments); // not used in this example
                    getFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();

                }

                else if (id.equals("9")) { // for option item 8, activity recognition fragment)

                    IndoorFragment fragment = new IndoorFragment();
                    // mapfragment.setArguments(arguments); // not used in this example
                    getFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();

                }

                else {

                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .add(R.id.item_detail_container, fragment)
                            .commit();


                }

        }
        else
        {
                if (id.equals("1")) { // for option item 2, use a map fragment
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID.
                    Intent mapDetailIntent = new Intent(this, ItemDetailMapActivity.class);
                    startActivity(mapDetailIntent);
                }

                else if (id.equals("2")) { // for option item 2, use a map fragment
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID.
                    Intent placesIntent = new Intent(this, placesActivity.class);
                    startActivity(placesIntent);
                }

                else if (id.equals("3")) { // for option item 2, use a map fragment
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID.
                    Intent geofenceIntent = new Intent(this, GeofenceActivity.class);
                    startActivity(geofenceIntent);
                }
                else if (id.equals("4")) { // for option item 2, use a map fragment
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID.
                    Intent wifiIntent = new Intent(this, WifiActivity.class);
                    startActivity(wifiIntent);
                }

                else if(id.equals("5"))
                {
                    Intent BluetoothIntent = new Intent(this,BluetoothActivity.class);
                    startActivity(BluetoothIntent);

                }


                else if(id.equals("6"))
                {

                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, SignInActivity.class));
                    finish();


                }


                else if(id.equals("7"))
                {
                    Intent picture = new Intent(this,MainActivity.class);
                    startActivity(picture);


                }

                else if(id.equals("8"))
                {
                    Intent recognition = new Intent(this, com.example.sengloke.InfoGo.ActivityRecognition.MainActivity.class);
                    startActivity(recognition);

                }

                else if(id.equals("9"))
                {
                    Intent indoor = new Intent(this,IndoorActivity.class);
                    startActivity(indoor);

                }



                else{
                    Intent detailIntent = new Intent(this, ItemDetailActivity.class);
                    detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
                    startActivity(detailIntent);
                }
        }
    }
}
