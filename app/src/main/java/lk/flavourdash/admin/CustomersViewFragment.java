package lk.flavourdash.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import lk.flavourdash.admin.Model.User;
import lk.flavourdash.admin.adapter.UserAdapter;

public class CustomersViewFragment extends Fragment{
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<User> users;
    private UserAdapter userAdapter;
    public static final String TAG = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customers_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        userAdapter = new UserAdapter(users, getContext());
        RecyclerView customerRecyclerView = view.findViewById(R.id.customerRecyclerView);
        customerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        customerRecyclerView.setAdapter(userAdapter);

        setupFirestoreListener();
    }

    private void setupFirestoreListener() {
        firebaseFirestore.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }

            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    User user = change.getDocument().toObject(User.class);
                    user.setDocumentId(change.getDocument().getId());

                    switch (change.getType()) {
                        case ADDED:
                            users.add(user);
                            break;
                        case MODIFIED:
                            updateModifiedBranch(change);
                            break;
                        case REMOVED:
                            users.removeIf(i -> i.getId().equals(user.getId()));
                            break;
                    }
                }

                userAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateModifiedBranch(DocumentChange change) {
        User updatedUser = change.getDocument().toObject(User.class);
        updatedUser.setDocumentId(change.getDocument().getId());

        for (int i = 0; i < users.size(); i++) {
            User existingUser = users.get(i);

            if (existingUser.getId().equals(updatedUser.getId())) {
                if (!existingUser.getFirstName().equals(updatedUser.getFirstName())) {
                    existingUser.setFirstName(updatedUser.getFirstName());
                }
                if (!existingUser.getLastName().equals(updatedUser.getLastName())) {
                    existingUser.setLastName(updatedUser.getLastName());
                }
                if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
                    existingUser.setEmail(updatedUser.getEmail());
                }
                if (!existingUser.getMobile().equals(updatedUser.getMobile())) {
                    existingUser.setMobile(updatedUser.getMobile());
                }
                if (!existingUser.isActive()==updatedUser.isActive()) {
                    existingUser.setActive(updatedUser.isActive());
                }

                userAdapter.notifyItemChanged(i);
                break;
            }
        }
    }
}