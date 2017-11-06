package hu.bme.mit.textmine.solr.configuration;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@Configuration
@EnableSolrRepositories(value = "hu.bme.mit.textmine.solr")
public class SolrContext {

    @Value("${solr.host}")
    private String solrHost;

    @Bean
    public SolrClient solrClient() throws MalformedURLException, IllegalStateException {
        return new HttpSolrClient.Builder(solrHost).build();
    }

    // @Bean
    // public SolrClient articleClient() throws MalformedURLException, IllegalStateException {
    // return new HttpSolrClient(solrHost);
    // }
    //
    // @Bean
    // public SolrClient formVariantClient() throws MalformedURLException, IllegalStateException {
    // return new HttpSolrClient(solrHost);
    // }
    //
    // @Bean
    // public SolrClient inflectionClient() throws MalformedURLException, IllegalStateException {
    // return new HttpSolrClient(solrHost);
    // }

    // @Bean
    // @Qualifier("template")
    // public SolrOperations solrTemplate() throws MalformedURLException, IllegalStateException {
    // return new SolrTemplate(solrClient());
    // }

    // @Bean
    // public SolrOperations solrTemplate() throws MalformedURLException, IllegalStateException {
    // return new SolrTemplate(solrClient());
    // }

    @Bean
    public SolrOperations solrTemplate() throws MalformedURLException, IllegalStateException {
        return new SolrTemplate(solrClient());
    }

    // @Bean(name = "text-mine-article-template")
    // public SolrOperations articleTemplate() throws MalformedURLException, IllegalStateException {
    // return new SolrTemplate(new HttpSolrClient.Builder(solrHost + "/text-mine-article").build());
    // }
    //
    // @Bean(name = "text-mine-formvariant-template")
    // public SolrOperations formVariantTemplate() throws MalformedURLException, IllegalStateException {
    // return new SolrTemplate(new HttpSolrClient.Builder(solrHost + "/text-mine-formvariant").build());
    // }
    //
    // @Bean(name = "text-mine-inflection-template")
    // public SolrOperations inflectionTemplate() throws MalformedURLException, IllegalStateException {
    // return new SolrTemplate(new HttpSolrClient.Builder(solrHost + "/text-mine-inflection").build());
    // }
}
