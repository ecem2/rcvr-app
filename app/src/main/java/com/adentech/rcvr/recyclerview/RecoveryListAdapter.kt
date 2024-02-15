package com.adentech.rcvr.recyclerview


import com.adentech.rcvr.adapter.RecoveryBaseListAdapter

abstract class RecoveryListAdapter<T : Any>(
    itemsSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem },
    contentsSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem }
) : RecoveryBaseListAdapter<T>(itemsSame, contentsSame)