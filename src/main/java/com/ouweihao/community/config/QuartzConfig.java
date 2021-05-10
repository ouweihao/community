package com.ouweihao.community.config;

import com.ouweihao.community.quartz.AlphaJob;
import com.ouweihao.community.quartz.PostScoreRefresh;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * 将配置存入数据库，quartz再从数据库中读取信息在进行操作
 */
@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程：
    // 1.通过FactoryBean封装Bean的实例化过程
    // 2.将FactoryBean装配到Spring容器里
    // 3.将FactoryBean注入给其他的Bean
    // 4.其他的Bean得到的是改FactoryBean所管理的Bean对象实例

    // 配置JobDetail实例，可执行，将注解反注释

    // @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(AlphaJob.class);
        jobDetail.setName("alphaJob");
        jobDetail.setGroup("alphaJobGroup");
        // 是否持久化保存，即当该JobDetail的trigger失效后，是否仍保存这个Job
        jobDetail.setDurability(true);
        // 出错后是否能恢复
        jobDetail.setRequestsRecovery(true);
        return jobDetail;
    }

    // 配置Trigger，可通过SimpleTriggerFactoryBean或者CronTriggerFactoryBean进行设置
    // 其中SimpleTriggerFactoryBean是进行简单的任务
    // 而CronTriggerFactoryBean可以用简单的命令对复杂的任务进行配置

    //  实例，可执行，将注解反注释

    // @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        // 这个触发器可以触发哪个Job
        trigger.setJobDetail(alphaJobDetail);
        trigger.setName("alphaTrigger");
        trigger.setGroup("alphaTriggerGroup");
        // 设置任务隔多久重复执行一次，单位是ms
        trigger.setRepeatInterval(3000);
        trigger.setJobDataMap(new JobDataMap());
        return trigger;
    }

    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(PostScoreRefresh.class);
        jobDetail.setName("postScoreRefreshJob");
        jobDetail.setGroup("CommunityJobGroup");
        // 是否持久化保存，即当该JobDetail的trigger失效后，是否仍保存这个Job
        jobDetail.setDurability(true);
        // 出错后是否能恢复
        jobDetail.setRequestsRecovery(true);
        return jobDetail;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        // 这个触发器可以触发哪个Job
        trigger.setJobDetail(postScoreRefreshJobDetail);
        trigger.setName("postScoreRefreshTrigger");
        trigger.setGroup("communityTriggerGroup");
        // 设置任务隔多久重复执行一次，单位是ms
        trigger.setRepeatInterval(1000 * 60 * 5);
        trigger.setJobDataMap(new JobDataMap());
        return trigger;
    }

}
