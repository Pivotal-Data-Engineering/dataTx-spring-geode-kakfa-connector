package io.pivotal.services.dataTx.geodekakfaconnector;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;

/**
 * @author Gregory Green
 */
@RestController("/jobs")
@Service
public class JobRestController
{


    //@Autowired
    //sprivate Source source;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("fromKakfaToGeodePdxStep")
    Step fromKakfaToGeodePdxStep;


    @Autowired
    @Qualifier("fromGeodePdxToKakfaStep")
    Step fromGeodePdxToKakfaStep;

    @GetMapping("startGeodePdxToKakfaJob/{geodeRegion}/{valueClassName}/{kafkaTopic}")
    public void startGeodePdxToKakfaJob(@PathVariable String geodeRegion,
                                        @PathVariable String valueClassName,
                                        @PathVariable String kafkaTopic) throws Exception{

        JobParametersBuilder b = new JobParametersBuilder()
                .addString("region",geodeRegion)
                .addString("topic",kafkaTopic)
                .addString("valueClassName",valueClassName)
                .addDate("time", Calendar.getInstance().getTime());


       Job job = this.jobBuilderFactory.get("startGeodePdxToKakfaJob")
               .flow(this.fromGeodePdxToKakfaStep)
               .end().build();

        jobLauncher.run(job,b.toJobParameters() );
    }//-------------------------------------------

    /**
     * Start Job to consume from Kaka and write to Geode
     * @param geodeRegion the Geode data store name
     * @param valueClassName the region value object type
     * @param kafkaTopic the kafka Topic
     * @throws Exception when an Internal error occurs
     */
    @GetMapping("startKakaToGeodePdxJob/{geodeRegion}/{valueClassName}/{kafkaTopic}")
    public void startKakaToGeodePdxJob(@PathVariable String geodeRegion,
                                        @PathVariable String valueClassName,
                                        @PathVariable String kafkaTopic)
    throws Exception
    {

        JobParametersBuilder b = new JobParametersBuilder()
                .addString("region",geodeRegion)
                .addString("topic",kafkaTopic)
                .addString("valueClassName",valueClassName)
                .addDate("time", Calendar.getInstance().getTime());


        Job job = this.jobBuilderFactory.get("startKakaToGeodePdxJob")
                .flow(this.fromKakfaToGeodePdxStep)
                .end().build();

        jobLauncher.run(job,b.toJobParameters() );
    }
}
