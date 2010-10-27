package org.codeartisans.staticlet;

public class StaticletConfiguration
{

    private final String docRoot;
    private final Boolean directoryListing;
    private final Integer bufferSize;
    private final Long expireTime;

    public StaticletConfiguration( String docRoot, Boolean directoryListing, Integer bufferSize, Long expireTime )
    {
        this.docRoot = docRoot;
        this.directoryListing = directoryListing;
        this.bufferSize = bufferSize;
        this.expireTime = expireTime;
    }

    public String getDocRoot()
    {
        return docRoot;
    }

    public boolean isDirectoryListing()
    {
        return directoryListing;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public long getExpireTime()
    {
        return expireTime;
    }

}
