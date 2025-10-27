<#-- CMSearchDescription.ftl -->
<?xml version="1.0" encoding="UTF-8"?>
<CMSearchDescription>
    <searchID>${searchID}</searchID>
    <trackList>
    <#list trackIds as tid>
        <trackID>${tid}</trackID>
    </#list>
    </trackList>
    <timeSpanList>
        <timeSpan>
            <startTime>${startTime}</startTime>
            <endTime>${endTime}</endTime>
        </timeSpan>
    </timeSpanList>
    <maxResults>${maxResults!100}</maxResults>
    <searchResultPostion>${searchResultPostion!0}</searchResultPostion>
    <metadataList>
        <metadataDescriptor>//recordType.meta.std-cgi.com</metadataDescriptor>
    </metadataList>
</CMSearchDescription>
