package com.example.photo.adapter;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo.R;
import com.example.photo.bean.AlarmClockItemInfo;
import com.example.photo.utils.FormatUtil;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;

import static com.example.photo.utils.Constants.FALSE;
import static com.example.photo.utils.Constants.TRUE;

public class AlarmClockAdapter extends RecyclerView.Adapter<AlarmClockAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener{
    private static final String TAG = "AlarmClockAdapter";
    private List<AlarmClockItemInfo> mAlarmList;
    private AlarmClockAdapter.OnItemClickListener mOnItemClickListener = null;

    //define interface
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View alarmClockItemView;
        TextView alarmClockTime;
        TextView alarmClockRepeat;
        TextView alarmClockLabel;
        SwitchButton switchButton;
        public ViewHolder(View itemView) {
            super(itemView);
            alarmClockItemView = itemView;
            alarmClockTime = itemView.findViewById(R.id.clock_time);
            alarmClockRepeat = itemView.findViewById(R.id.clock_repeat);
            alarmClockLabel =  itemView.findViewById(R.id.clock_label);
            switchButton = itemView.findViewById(R.id.clock_switch);
        }
    }

    public AlarmClockAdapter(List<AlarmClockItemInfo> mAlarmList) {
        this.mAlarmList = mAlarmList;
    }

    public void setOnItemClickListener(AlarmClockAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public AlarmClockAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_clock_item, parent,false);
        final AlarmClockAdapter.ViewHolder holder = new AlarmClockAdapter.ViewHolder(view);
        holder.alarmClockItemView.setOnClickListener(this);
        holder.alarmClockItemView.setOnLongClickListener(this);
        holder.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = holder.getAdapterPosition();
                if(isChecked){
                    mAlarmList.get(position).setIsEnable(TRUE);
                    mAlarmList.get(position).save();
                }else{
                    mAlarmList.get(position).setIsEnable(FALSE);
                    mAlarmList.get(position).save();
                }
            }
        });
        return holder;
    }

    @Override
    public boolean onLongClick(View v) {
        int position = (int)v.getTag();
        //long uid = mAlarmList.get(position).getUid();
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemLongClick(position);
        }else{
            Log.e(TAG,"you forgot add listener!");
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int position = (int)v.getTag();
        //long uid = mAlarmList.get(position).getUid();
        if(mOnItemClickListener != null){
            mOnItemClickListener.onItemClick(position);
        }else{
            Log.e(TAG,"you forgot add listener!");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmClockAdapter.ViewHolder holder, int position) {
        AlarmClockItemInfo alarmClockItemInfo = mAlarmList.get(position);
        holder.alarmClockTime.setText(
                FormatUtil.addTimeSeparator(alarmClockItemInfo.getHour(),alarmClockItemInfo.getMinute()));
        holder.alarmClockLabel.setText(FormatUtil.lessThan8Words(alarmClockItemInfo.getLabel()));
        holder.alarmClockRepeat.setText(FormatUtil.getRepeatText(alarmClockItemInfo.getRepeat()));

        if(alarmClockItemInfo.getIsEnable() == TRUE){
            holder.switchButton.setChecked(true);
        } else{
            holder.switchButton.setChecked(false);
        }
        holder.alarmClockItemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mAlarmList.size();
    }
}
