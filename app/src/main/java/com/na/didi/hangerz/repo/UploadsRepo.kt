package com.na.didi.hangerz.repo

import com.na.didi.hangerz.db.dao.UploadsDao
import javax.inject.Inject

class UploadsRepo @Inject constructor(
        private val uploadsDao: UploadsDao){
}