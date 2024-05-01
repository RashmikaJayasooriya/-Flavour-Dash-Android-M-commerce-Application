package lk.flavourdash.admin.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import lk.flavourdash.admin.Model.User;
import lk.flavourdash.admin.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private ArrayList<User> data;
    private Context context;
    private FirebaseFirestore firestore;



    public UserAdapter(ArrayList<User> data, Context context) {
        this.data = data;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Nonnull
    @Override
    public MyViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_view, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@Nonnull MyViewHolder holder, int position) {
        User item = data.get(position);

        holder.userName.setText(item.getFirstName()+" "+item.getLastName());
        holder.userEmail.setText(item.getEmail());
        holder.userMobile.setText(item.getMobile());


        holder.userStatus.setOnCheckedChangeListener(null);
        holder.userStatus.setChecked(item.isActive());
        holder.userStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != item.isActive()) {
                String documentId = item.getDocumentId();
                DocumentReference userReference = firestore.collection("users").document(documentId);

                userReference.update("active", isChecked)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update successful
                                Toast.makeText(context, "DocumentSnapshot successfully updated!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle errors
                                Toast.makeText(context, "Error updating document", Toast.LENGTH_SHORT).show();
                                Log.e("FirestoreUpdateError", "Error updating document", task.getException());
                            }
                        });
            }
        });
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userEmail;
        TextView userMobile;
        MaterialSwitch userStatus;

        MyViewHolder(View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.userName);
            userEmail=itemView.findViewById(R.id.userEmail);
            userMobile=itemView.findViewById(R.id.userMobile);
            userStatus=itemView.findViewById(R.id.userStatus);
        }
    }
}
