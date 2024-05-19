package com.example.myapplicationkurs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.myapplicationkurs.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationkurs.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button logout_btn;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    ////////////////////////////////////////////////для лист
    TextView tvSign;
    public static TextView tvEmpty, tvBalance;
    EditText etAmount, etMessage;
    ImageView ivSend;
    boolean positive = true;
    RecyclerView rvTransactions;
    TransactionAdapter adapter;
    ArrayList<TransactionClass> transactionList;

    ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();

        transactionList = new ArrayList<TransactionClass>();
        loadData();
        tvSign = binding.tvSign;
        rvTransactions = binding.rvTransactions;
        etAmount = binding.etAmount;
        etMessage = binding.etMessage;
        ivSend = binding.ivSend;
        tvEmpty = binding.tvEmpty;




        setSupportActionBar(findViewById(R.id.custom_toolbar));

        // Вызов метода для установки настраиваемого action bar
        setCustomActionBar();

        // Function to initialize views
        initViews();

        // Function to load data from shared preferences
        loadData();

        // Function to set custom action bar
        setCustomActionBar();

        // To check if there is no transaction
        Log.d("boolshiet2",transactionList.toString());
        checkIfEmpty(transactionList.size());

        // Initializing recycler view
        rvTransactions.setHasFixedSize(true);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, transactionList);
        Log.d("boolshiet3",transactionList.toString());
        rvTransactions.setAdapter(adapter);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                binding.rvTransactions.setVisibility(View.VISIBLE);
                binding.enter.setVisibility(View.VISIBLE);
                binding.customToolbar.setVisibility(View.VISIBLE);
                binding.frameLayout.setVisibility(View.GONE);
            } else if (itemId == R.id.graph) {
                binding.rvTransactions.setVisibility(View.GONE);
                binding.enter.setVisibility(View.GONE);
                binding.customToolbar.setVisibility(View.GONE);
                binding.frameLayout.setVisibility(View.VISIBLE);
                openGraphFragment(); // Вызываем метод без параметров
            } else if (itemId == R.id.information) {
                binding.rvTransactions.setVisibility(View.GONE);
                binding.enter.setVisibility(View.GONE);
                binding.customToolbar.setVisibility(View.GONE);
                binding.frameLayout.setVisibility(View.VISIBLE);
                replaceFragment(new info());
            } else if (itemId == R.id.exit) {
                saveData();
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            return true;

        });




        // On click sign change
        tvSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSign();
            }
        });

        // On click Send
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Input Validation
                if (etAmount.getText().toString().trim().isEmpty()) {
                    etAmount.setError("Введите сумму!");
                    return;
                }
                if (etMessage.getText().toString().isEmpty()) {
                    etMessage.setError("Введите сообщение!");
                    return;
                }
                try {
                    int amt = Integer.parseInt(etAmount.getText().toString().trim());

                    // Adding Transaction to recycler View
                    sendTransaction(amt, etMessage.getText().toString().trim(), positive);
                    checkIfEmpty(transactionList.size());
                    saveData();

                    // To update Balance
                    setBalance(transactionList);
                    etAmount.setText("");
                    etMessage.setText("");
                } catch (Exception e) {
                    etAmount.setError("Сумма должна быть целым числом больше нуля!");
                }

            }
        });

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
    // To set custom action bar
    private void setCustomActionBar() {
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        View v = LayoutInflater.from(this).inflate(R.layout.custom_action_bar,null);

        // TextView to show Balance
        tvBalance = v.findViewById(R.id.tvBalance);

        // Setting balance
        setBalance(transactionList);
        Log.d("loadDatafinalbalance", "Transaction List: " + transactionList.toString());
        getSupportActionBar().setCustomView(v);
        getSupportActionBar().setElevation(0);
    }

    // To set Balance along with sign (spent(-) or received(+))
    public static void setBalance(ArrayList<TransactionClass> transactionList){
        int bal = calculateBalance(transactionList);
        if(bal<0)
        {
            tvBalance.setText("-₽"+calculateBalance(transactionList)*-1);
            Log.d("loadDatafinalbalance+", "Transaction List: " + transactionList.toString());
        }
        else {
            tvBalance.setText("+₽"+calculateBalance(transactionList));
            Log.d("loadDatafinalbalance-", "Transaction List: " + transactionList.toString());
        }
    }

//     To load data from shared preference
//    private void loadData() {
//        SharedPreferences pref = getSharedPreferences("com.cs.ec",MODE_PRIVATE);
//        Gson gson = new Gson();
//        String json = pref.getString("transactions",null);
//        Type type = new TypeToken<ArrayList<TransactionClass>>(){}.getType();
//        if(json!=null)
//        {
//            transactionList=gson.fromJson(json,type);
//        }
//    }

    private void loadData() {
        ref.child("Users").child(mAuth.getUid()).child("transactions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String json = snapshot.getValue().toString();
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<TransactionClass>>(){}.getType();
                    Log.d("loadData", "JSON: " + json);
                    if (json != null) {
                        transactionList = gson.fromJson(json, type);
                        Log.d("loadData", "Transaction List: " + transactionList.toString());
                        // Notify the adapter that data has changed
                        adapter.notifyDataSetChanged();
                        // Check if the transaction list is empty
                        checkIfEmpty(transactionList.size());

                        rvTransactions.setHasFixedSize(true);
                        rvTransactions.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        adapter = new TransactionAdapter(MainActivity.this, transactionList);
                        Log.d("boolshiet3",transactionList.toString());
                        rvTransactions.setAdapter(adapter);
                        setCustomActionBar();

                    }

                } else {
                    Log.d("loadData", "No transactions found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("loadData", "Database error: " + error.getMessage());
            }
        });
        Log.d("loadDatafinal", "Transaction List: " + transactionList.toString());
    }


    // To add transaction
    private void sendTransaction(int amt,String msg, boolean positive) {
        transactionList.add(new TransactionClass(amt,msg,positive));
        adapter.notifyDataSetChanged();
        rvTransactions.smoothScrollToPosition(transactionList.size()-1);
    }

    // Function to change sign
    private void changeSign() {
        if(positive)
        {
            tvSign.setText("-₽");
            tvSign.setTextColor(Color.parseColor("#F44336"));
            positive = false;
        }
        else {
            tvSign.setText("+₽");
            tvSign.setTextColor(Color.parseColor("#00c853"));
            positive = true;
        }
    }

    // To check if transaction list is empty
    public static void checkIfEmpty(int size) {
        if (size == 0)
        {
            MainActivity.tvEmpty.setVisibility(View.VISIBLE);
        }
        else {
            MainActivity.tvEmpty.setVisibility(View.GONE);
        }
    }

    // To Calculate Balance by iterating through all transactions
    public static int calculateBalance(ArrayList<TransactionClass> transactionList)
    {
        int bal = 0;
        for(TransactionClass transaction : transactionList)
        {
            if(transaction.isPositive())
            {
                bal+=transaction.getAmount();
            }
            else {
                bal-=transaction.getAmount();
            }
        }
        return bal;
    }

    // Initializing Views
    private void initViews() {
        transactionList = new ArrayList<TransactionClass>();
        tvSign = binding.tvSign;
        rvTransactions = binding.rvTransactions;
        etAmount = binding.etAmount;
        etMessage = binding.etMessage;
        ivSend = binding.ivSend;
        tvEmpty = binding.tvEmpty;
    }

    // Storing data locally
    // using shared preferences
    // in onStop() method
//    @Override
//    protected void onStop() {
//        super.onStop();
//        SharedPreferences.Editor editor = getSharedPreferences("com.cs.ec",MODE_PRIVATE).edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(transactionList);
//        editor.putString("transactions",json);
//        editor.apply();
//    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();  // Save data to Firebase when the activity is stopped
    }
    @Override
    protected void onPause() {

        super.onPause();
        saveData();
    }

    private void saveData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Gson gson = new Gson();
            String json = gson.toJson(transactionList);
            ref.child("Users").child(mAuth.getUid()).child("transactions").setValue(json);
        }
    }


    private void openGraphFragment() {
        // Подсчитываем положительные и отрицательные значения
        int[] values = calculateTransactionValues();
        int positiveValue = values[0];
        int negativeValue = values[1];

        // Создаем новый экземпляр фрагмента и передаем данные
        GraphFragment graphFragment = GraphFragment.newInstance(negativeValue, positiveValue);

        // Заменяем текущий фрагмент на GraphFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, graphFragment);
        fragmentTransaction.commit();
    }


    private int[] calculateTransactionValues() {
        int positiveSum = 0;
        int negativeSum = 0;
        for (TransactionClass transaction : transactionList) {
            if (transaction.isPositive()) {
                positiveSum += transaction.getAmount();
            } else {
                negativeSum += transaction.getAmount();
            }
        }
        return new int[]{positiveSum, negativeSum};
    }


}

