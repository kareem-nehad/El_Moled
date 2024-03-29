package com.elmoledmol.www;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class myCartActivity extends AppCompatActivity {
    RecyclerView recyclerCart;
    CartAdapter adapter;
    CardView card;
    Button button;
    ImageView back;
    List<adressinheret> listAddresses = new ArrayList<>();
    ArrayList<cartinheret> list = new ArrayList<>();
    ArrayList<featuredinheret> listFavs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);
        getSupportActionBar().hide();
        button = findViewById(R.id.button);
        back = findViewById(R.id.backCart);
        recyclerCart = findViewById(R.id.recyclerCart);


//        for (int i=0;i<loadData().size();i++) {
//            System.out.println(loadData().get(i).getColorId()+" <---- Color ID in cart");
//            System.out.println(loadData().get(i) .getSizeId()+ " <---- Size ID in cart");
//            System.out.println(list.get(i).getName());
//        }




        // Showing the data in the cart.
        adapter = new CartAdapter(loadData(),getApplicationContext());
        adapter.notifyDataSetChanged();
        recyclerCart.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerCart.setAdapter(adapter);

        card = findViewById(R.id.card99);
        card.setBackgroundResource(R.drawable.corner);

        // Setting the swipe detector.
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerCart);

        // To Proceed to Checkout.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(loadData().size()==0){
                    Toast.makeText(myCartActivity.this, "You need to add some items", Toast.LENGTH_SHORT).show();
                }
                else {
               Intent intent=new Intent(myCartActivity.this,ordercheckout.class);
               startActivity(intent);}
            }
        });


        // To go back to previous page.
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = list.size();
                Intent intent = new Intent(myCartActivity.this, MainActivity.class);
                intent.putExtra("count", count);
                startActivity(intent);
            }
        });

//        Intent intent = getIntent();
//        System.out.println(intent.getStringExtra("productName"));
    }


    private List<cartinheret> loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences2", 0);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("product",null);
        Type type = new TypeToken<ArrayList<cartinheret>>() {
        }.getType();
        list = gson.fromJson(json, type);

        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }



    private void saveData(List<cartinheret> list) {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences2",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("product", json);
        editor.apply();
    }



    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();
            int lastPosition = list.size() - 1;

            switch (direction) {
                case ItemTouchHelper.LEFT:
                    cartinheret deletedRecord = new cartinheret(list.get(position).name, list.get(position).price, list.get(position).images,list.get(position).mainId, list.get(position).Qunatity,list.get(position).colorId,list.get(position).sizeId,list.get(position).percentage);
                    if (position == lastPosition) {
                        list.remove(lastPosition);
                        adapter.notifyItemRemoved(lastPosition);
                        adapter.notifyDataSetChanged();

                        saveData(list);

                        Snackbar.make(recyclerCart, "Item deleted successfully", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        list.add(position, deletedRecord);
                                        adapter.notifyItemInserted(position);
                                        adapter.notifyDataSetChanged();

                                        saveData(list);
                                    }
                                }).show();
                        break;

                    } else {

                        list.remove(position);
                        adapter.notifyItemRemoved(position);
                        saveData(list);


                        Snackbar.make(recyclerCart, "Item deleted successfully", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        list.add(position,deletedRecord);
                                        adapter.notifyItemInserted(position);
                                        adapter.notifyDataSetChanged();

                                        saveData(list);
                                    }
                                }).show();
                        break;
                    }

                    case ItemTouchHelper.RIGHT:
                        featuredinheret toBeinFavs = new featuredinheret(list.get(position).name,String.valueOf(list.get(position).price),"0",list.get(position).images,String.valueOf(list.get(position).percentage));
                        listFavs.clear();
                        listFavs.addAll(loadDataFavs());
                        listFavs.add(toBeinFavs);
                        adapter.notifyDataSetChanged();
                        saveDataFavs(listFavs);

                        Snackbar.make(recyclerCart, "Item added to bookmarks successfully",Snackbar.LENGTH_LONG).show();

                        break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(getApplicationContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(0xFFDA1E1E)
                    .addSwipeLeftActionIcon(R.drawable.delete)
                    .addSwipeRightBackgroundColor(0xFFE4C105)
                    .addSwipeRightActionIcon(R.drawable.favorite_cart)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    public void onBackPressed() {
        startActivity(new Intent(myCartActivity.this,MainActivity.class));
    }

    private void saveDataFavs(List<featuredinheret> list) {
        SharedPreferences sharedPreferences = getSharedPreferences("Bookmarks", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("bookmark", json);
        editor.apply();
    }

    private List<featuredinheret> loadDataFavs() {
        SharedPreferences sharedPreferences = getSharedPreferences("Bookmarks", 0);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("bookmark", null);
        Type type = new TypeToken<ArrayList<featuredinheret>>() {
        }.getType();
        listFavs = gson.fromJson(json, type);

        if (listFavs == null) {
            listFavs = new ArrayList<>();
        }

        return listFavs;
    }
}