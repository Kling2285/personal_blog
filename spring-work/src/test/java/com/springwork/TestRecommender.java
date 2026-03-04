package com.springwork;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.*;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TestRecommender {

    @Test
    public void test() throws Exception {
        //一，构建数据模型DataModel
        //1.用户端偏好数据
        List<Preference> list1=new ArrayList<>();//用户1
        list1.add(new GenericPreference(1L,2L,5.6f));
        list1.add(new GenericPreference(1L,3L,6.6f));
        list1.add(new GenericPreference(1L,4L,2.6f));

        List<Preference> list2=new ArrayList<>();//用户2
        list2.add(new GenericPreference(2L,3L,4.6f));
        list2.add(new GenericPreference(2L,4L,2.6f));
        list2.add(new GenericPreference(2L,5L,7.6f));

        List<Preference> list3=new ArrayList<>();//用户3
        list3.add(new GenericPreference(3L,4L,8.6f));
        list3.add(new GenericPreference(3L,5L,4.6f));
        list3.add(new GenericPreference(3L,6L,5.6f));

        //存储多个用户端偏好集合
        FastByIDMap<PreferenceArray> map=new FastByIDMap<>();

        map.put(1L,new GenericUserPreferenceArray(list1));
        map.put(2L,new GenericUserPreferenceArray(list2));
        map.put(3L,new GenericUserPreferenceArray(list3));

        //构建数据模型
        DataModel dataModel=new GenericDataModel(map);

        //二，构建相似度
        UserSimilarity similarity=new EuclideanDistanceSimilarity(dataModel);

        //三，定义邻域
        UserNeighborhood neighborhood=new NearestNUserNeighborhood(10,similarity,dataModel);
        long[] longs = neighborhood.getUserNeighborhood(1L);
        System.out.println("================和用户喜好类似的邻居=================");
        for(long along:longs){
            System.out.println(along);
        }
        //四，定义推荐器
        Recommender recommender=new GenericUserBasedRecommender(dataModel,neighborhood,similarity);
        List<RecommendedItem> recommendedItemsList= recommender.recommend(1,4);
        System.out.println("================推荐的商品=================");
        recommendedItemsList.forEach(item -> {
            System.out.println(item.getItemID()+"\t"+item.getValue());
        });

    }

}
