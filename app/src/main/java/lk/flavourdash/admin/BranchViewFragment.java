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

import lk.flavourdash.admin.Model.Branch;
import lk.flavourdash.admin.adapter.BranchAdapter;
import lk.flavourdash.admin.listener.OnItemClickListener;

public class BranchViewFragment extends Fragment implements OnItemClickListener<Branch> {
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<Branch> branches;
    private BranchAdapter branchAdapter;
    public static final String TAG = MainActivity.class.getName();
    public BranchViewFragment() {
        // empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_branch_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        branches = new ArrayList<>();
        branchAdapter = new BranchAdapter(branches, getContext(),this);
        RecyclerView branchRecyclerView = view.findViewById(R.id.branchRecyclerView);
        branchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        branchRecyclerView.setAdapter(branchAdapter);

        setupFirestoreListener();
    }

    private void setupFirestoreListener() {
        firebaseFirestore.collection("restaurant_branches").orderBy("name").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting documents: ", error);
                return;
            }

            if (value != null) {
                for (DocumentChange change : value.getDocumentChanges()) {
                    Branch branch = change.getDocument().toObject(Branch.class);
                    branch.setId(change.getDocument().getId());

                    switch (change.getType()) {
                        case ADDED:
                            branches.add(branch);
                            break;
                        case MODIFIED:
                            updateModifiedBranch(change);
                            break;
                        case REMOVED:
                            branches.removeIf(i -> i.getId().equals(branch.getId()));
                            break;
                    }
                }

                branchAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateModifiedBranch(DocumentChange change) {
        Branch updatedBranch = change.getDocument().toObject(Branch.class);
        updatedBranch.setId(change.getDocument().getId());

        for (int i = 0; i < branches.size(); i++) {
            Branch existingBranch = branches.get(i);

            if (existingBranch.getId().equals(updatedBranch.getId())) {
                if (!existingBranch.getName().equals(updatedBranch.getName())) {
                    existingBranch.setName(updatedBranch.getName());
                }

                if (!existingBranch.getLatitude().equals(updatedBranch.getLatitude())) {
                    existingBranch.setLatitude(updatedBranch.getLatitude());
                }

                if (!existingBranch.getLongitude().equals(updatedBranch.getLongitude())) {
                    existingBranch.setLongitude(updatedBranch.getLongitude());
                }

                branchAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onItemClick(Branch branch) {
        BranchUpdateFragment dialog = BranchUpdateFragment.newInstance(branch);
        dialog.show(getChildFragmentManager(), "FullScreenDialog");
    }
}