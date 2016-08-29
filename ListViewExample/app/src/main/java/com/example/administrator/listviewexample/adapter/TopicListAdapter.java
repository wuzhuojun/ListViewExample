package com.example.administrator.listviewexample.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.administrator.listviewexample.R;
import com.example.administrator.listviewexample.model.TopicModel;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/8/26.
 */
public class TopicListAdapter extends BaseAdapter {
    private ArrayList<TopicModel> mDatas = new ArrayList<TopicModel>();
    private Context mContext;

    public TopicListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addDatas(List<TopicModel> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void clear() {
        mDatas.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.topic_list_item, null);

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTopicTitle.setText(mDatas.get(position).getTitle());
        String imgUrl = mDatas.get(position).getPic_path();
        if (null != imgUrl) {
            Uri imgUri = Uri.parse(imgUrl);
            holder.mTopicImage.setImageURI(imgUri);
        }


        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.topic_title)
        TextView mTopicTitle;

        @BindView(R.id.topic_image)
        SimpleDraweeView mTopicImage;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
