package com.bosu.housebook.asset;

/** REAL_ESTATE 자산이 자가(소유)인지, 전세인지, 월세인지 구분한다. 전세는 manualValue를
 * 전세보증금으로, 월세는 manualValue를 보증금 + monthlyRent를 월세로 쓴다. */
public enum RealEstateCategory {
    OWNED,
    JEONSE,
    WOLSE
}
