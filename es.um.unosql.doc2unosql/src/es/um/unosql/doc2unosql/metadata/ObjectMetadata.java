package es.um.unosql.doc2unosql.metadata;

public class ObjectMetadata
{
    private long count;
    private long firstTimestamp;
    private long lastTimestamp;

    public ObjectMetadata()
    {
    }

    public ObjectMetadata(long count, long firstTimestamp, long lastTimestamp)
    {
        this.count = count;
        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = lastTimestamp;
    }

    public long getCount()
    {
        return count;
    }

    public void setCount(long count)
    {
        this.count = count;
    }

    public long getFirstTimestamp()
    {
        return firstTimestamp;
    }

    public void setFirstTimestamp(long firstTimestamp)
    {
        this.firstTimestamp = firstTimestamp;
    }

    public long getLastTimestamp()
    {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp)
    {
        this.lastTimestamp = lastTimestamp;
    }

    public void combineMetadata(ObjectMetadata orig)
    {
        count += orig.count;

        if (firstTimestamp == 0 || orig.firstTimestamp < firstTimestamp)
            firstTimestamp = orig.firstTimestamp;

        if (lastTimestamp == 0 || orig.lastTimestamp > lastTimestamp)
            lastTimestamp = orig.lastTimestamp;
    }
}
