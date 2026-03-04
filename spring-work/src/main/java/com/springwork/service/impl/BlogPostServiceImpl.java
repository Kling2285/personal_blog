package com.springwork.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springwork.entity.BlogPost;
import com.springwork.entity.Comment;
import com.springwork.mapper.BlogPostMapper;
import com.springwork.mapper.CommentMapper;
import com.springwork.service.BlogPostService;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlogPostServiceImpl extends ServiceImpl<BlogPostMapper, BlogPost> implements BlogPostService {
    @Autowired
    private CommentMapper commentMapper;

    // 新增：带count参数的随机帖子方法（核心，绕开分页插件）
    private List<BlogPost> getRandomPostList(int count) {
        // 1. 查询所有随机帖子（分页插件拦截也能获取全量后截取）
        QueryWrapper<BlogPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("rand()");
        List<BlogPost> allRandomPosts = baseMapper.selectList(queryWrapper);

        // 2. 循环截取/补充，确保返回count数量（允许重复）
        List<BlogPost> resultList = new ArrayList<>();
        int index = 0;
        while (resultList.size() < count) {
            // 取模避免数组越界，循环添加（允许重复）
            resultList.add(allRandomPosts.get(index % allRandomPosts.size()));
            index++;
        }
        return resultList;
    }

    // 保留原有无参方法（兼容其他潜在调用）
    private List<BlogPost> getRandomPostList() {
        return getRandomPostList(10); // 无参时默认返回10条
    }

    @Override
    public List<BlogPost> selectRecommendList(Integer userId, int count) {
        // 未登录 → 返回指定数量随机帖子
        if (userId == null) {
            return getRandomPostList(count);
        } else {
            // 已登录但无发帖/评论 → 返回指定数量随机帖子
            QueryWrapper<BlogPost> postQuery = new QueryWrapper<>();
            postQuery.eq("user_id", userId);
            long postCount = baseMapper.selectCount(postQuery);

            QueryWrapper<Comment> commentQuery = new QueryWrapper<>();
            commentQuery.eq("user_id", userId);
            long commentCount = commentMapper.selectCount(commentQuery);

            if (postCount == 0 && commentCount == 0) {
                return getRandomPostList(count);
            }

            // 有发帖/评论 → 走协同过滤推荐
            return this.recommendBlogPostList(userId, count);
        }
    }

    private long getCategoryPostCount(Integer userId, Integer categoryId) {
        QueryWrapper<BlogPost> categoryCountQuery = new QueryWrapper<>();
        categoryCountQuery.eq("user_id", userId)
                .eq("category_id", categoryId);
        return baseMapper.selectCount(categoryCountQuery);
    }

    private List<BlogPost> recommendBlogPostList(Integer userId, int count) {
        FastByIDMap<PreferenceArray> map = new FastByIDMap<>();

        // 1. 构建协同过滤数据模型（保留原有核心逻辑）
        QueryWrapper<BlogPost> userPostQuery = new QueryWrapper<>();
        userPostQuery.eq("user_id", userId);
        List<BlogPost> userAllPosts = baseMapper.selectList(userPostQuery);

        userAllPosts.forEach(item -> {
            Long UserId = userId.longValue();
            Integer postId = item.getId();
            Integer categoryId = item.getCategoryId();
            long categoryPostCount = getCategoryPostCount(userId, categoryId);
            Preference preference = new GenericPreference(UserId, postId, categoryPostCount);

            if (map.containsKey(UserId)) {
                GenericUserPreferenceArray userPref = (GenericUserPreferenceArray) map.get(UserId);
                // 扩容数组避免越界
                GenericUserPreferenceArray newUserPref = new GenericUserPreferenceArray(userPref.length() + 1);
                for (int i = 0; i < userPref.length(); i++) {
                    newUserPref.set(i, userPref.get(i));
                }
                newUserPref.set(userPref.length(), preference);
                newUserPref.setUserID(0, UserId);
                map.put(UserId, newUserPref);
            } else {
                GenericUserPreferenceArray userPref = new GenericUserPreferenceArray(userAllPosts.size());
                userPref.setUserID(0, UserId);
                userPref.set(0, preference);
                map.put(UserId, userPref);
            }
        });
        DataModel dataModel = new GenericDataModel(map);

        try {
            // 2. 协同过滤核心逻辑（保留原有）
            UserSimilarity similarity = new EuclideanDistanceSimilarity(dataModel);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, dataModel);
//            long[] similarUserIds = neighborhood.getUserNeighborhood(userId);
//            for (long similarUserId : similarUserIds) {
//                System.out.println("相似用户ID：" + similarUserId);
//            }

            Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
            List<RecommendedItem> recommendedItemsList = recommender.recommend(userId, count);

            // 3. 处理协同过滤结果
            List<BlogPost> recommendList = new ArrayList<>();
            if (!recommendedItemsList.isEmpty()) {
                // 提取推荐的帖子ID并查询
                List<Long> recommendIds = new ArrayList<>();
                recommendedItemsList.forEach(item -> {
                    recommendIds.add(item.getItemID());
//                    System.out.println("协同过滤推荐帖子ID：" + item.getItemID());
                });

                // 转换ID类型（Long→Integer），查询帖子
                List<Integer> intIds = recommendIds.stream().map(Long::intValue).toList();
                List<BlogPost> cfPosts = baseMapper.selectBatchIds(intIds);

                // 添加协同过滤结果
                recommendList.addAll(cfPosts);
            }

            // 4. 补充随机帖子至总数=count（允许重复）
            int needRandomCount = count - recommendList.size();
            if (needRandomCount > 0) {
                recommendList.addAll(getRandomPostList(needRandomCount));
            }

            // 兜底：确保最终数量严格等于count（防止极端情况）
            if (recommendList.size() < count) {
                recommendList.addAll(getRandomPostList(count - recommendList.size()));
            }

            return recommendList;

        } catch (TasteException e) {
            // 协同过滤报错 → 降级返回指定数量随机帖子
            e.printStackTrace();
            return getRandomPostList(count);
        }
    }
}