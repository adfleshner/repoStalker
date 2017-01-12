package com.bypassmobile.octo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bypassmobile.octo.R;
import com.bypassmobile.octo.image.ImageLoader;
import com.bypassmobile.octo.model.User;
import com.squareup.picasso.NetworkPolicy;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by aaronfleshner on 1/11/17.
 */

public class StalkerAdapter extends RecyclerView.Adapter<StalkerAdapter.UserViewHolder> implements Filterable {

    //creates a simple interface for the adapter.
    public interface OnUserClickListener {
        void onUserClick(User user);
    }



    private ArrayList<User> mData;
    private ArrayList<User> mOriginalData;
    private OnUserClickListener mUserCallback;

    public StalkerAdapter(ArrayList<User> users) {
        this.mOriginalData = users;
        this.mData = users;
    }

    //sets the listener for the adpater
    public void setOnUserClickListener(OnUserClickListener listener) {
        this.mUserCallback = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_cell_user, parent, false));
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        holder.bindView(getItem(position));
    }


    //gets an item from the displayed data on the screen
    private User getItem(int pos) {
        return mData.get(pos);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    //Filters on Name
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                final List<User> list = mOriginalData;
                int count = list.size();
                final ArrayList<User> nList = new ArrayList<>(count);
                String filterableString;
                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i).getName();
                    if(filterableString.toLowerCase().contains(filterString)){
                        nList.add(list.get(i));
                    }
                }
                results.values = nList;
                results.count = nList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //publish the results and notify the data set.
                mData = (ArrayList<User>)filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView mUserAvatar;
        private TextView mUsername;

        public UserViewHolder(View itemView) {
            super(itemView);
            mUserAvatar = (ImageView) itemView.findViewById(R.id.cvUserAvatar);
            mUsername = (TextView) itemView.findViewById(R.id.tvUserName);
        }

        //Bind all of the data to the cell.
        private void bindView(final User user) {
            ImageLoader.createImageLoader(itemView.getContext())
                    .load(user.getProfileURL())
                    .placeholder(R.drawable.ic_bypass_logo)
                    .tag("User List")
                    .into(mUserAvatar);
            mUsername.setText(user.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if the call back is set send information to be processed elsewhere
                    if (mUserCallback != null) {
                        mUserCallback.onUserClick(user);
                        return;
                    }
                    //otherwise just return the name of the user.
                    Toast.makeText(itemView.getContext(), user.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
