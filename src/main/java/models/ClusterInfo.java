package models;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusterInfo {
    private int clusterId;
    private Date recency;
    private int totalPoints;
    private Date averageDate;
    private Double diameter;
    private Set<String> distinctRss;
    private int coverage;
    public double sc;

    public int getCoverage() {
        return coverage;
    }

    public double score(Date mostRecent) {
        Long recL = new Long(recency.getTime());
        Long mrL = new Long(mostRecent.getTime());
        double timeParam = recL.doubleValue() / mrL.doubleValue();
        Integer cI = new Integer(coverage);
        double coverageParam = cI.doubleValue()/10.0;
        double diaParam = diameter;
        sc = 5000000.0*timeParam+15.0*coverageParam + diaParam*20.0;
        return sc;
    }

    public void setCoverage(int coverage) {
        this.coverage = coverage;
    }

    public ClusterInfo(){
        distinctRss = new HashSet<>();
    }

    public void addRssLinks(List<String> rssList){
        distinctRss.addAll(rssList);
    }

    public void addRssLink(String rss){
        distinctRss.add(rss);
    }
    public Set<String> getDistinctRss() {
        return distinctRss;
    }

    public void setDistinctRss(Set<String> distinctRss) {
        this.distinctRss = distinctRss;
    }

    public Double getDiameter() {
        return diameter;
    }

    public void setDiameter(Double diameter) {
        this.diameter = diameter;
    }

    public int getClusterId() {
        return clusterId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Date getAverageDate() {
        return averageDate;
    }

    public void setAverageDate(Date averageDate) {
        this.averageDate = averageDate;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public Date getRecency() {
        return recency;
    }

    public void setRecency(Date recency) {
        this.recency = recency;
    }

}
