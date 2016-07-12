##################################################
# file: DaemonStatusServiceImplService_client.py
# 
# client stubs generated by "ZSI.generate.wsdl2python.WriteServiceModule"
#     /usr/bin/wsdl2py -b DaemonStatusService.wsdl
# 
##################################################

from DaemonStatusServiceImplService_types import *
import urlparse, types
from ZSI.TCcompound import ComplexType, Struct
from ZSI import client
from ZSI.schema import GED, GTD
import ZSI
from ZSI.generate.pyclass import pyclass_type

# Locator
class DaemonStatusServiceImplServiceLocator:
    DaemonStatusServiceImplPort_address = "http://localhost:8081/ws/DaemonStatusService"
    def getDaemonStatusServiceImplPortAddress(self):
        return DaemonStatusServiceImplServiceLocator.DaemonStatusServiceImplPort_address
    def getDaemonStatusServiceImplPort(self, url=None, **kw):
        return DaemonStatusServiceImplServiceSoapBindingSOAP(url or DaemonStatusServiceImplServiceLocator.DaemonStatusServiceImplPort_address, **kw)

# Methods
class DaemonStatusServiceImplServiceSoapBindingSOAP:
    def __init__(self, url, **kw):
        kw.setdefault("readerclass", None)
        kw.setdefault("writerclass", None)
        # no resource properties
        self.binding = client.Binding(url=url, **kw)
        # no ws-addressing

    # op: queryTdbAus
    def queryTdbAus(self, request, **kw):
        if isinstance(request, queryTdbAus) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryTdbAusResponse.typecode)
        return response

    # op: getPlatformConfiguration
    def getPlatformConfiguration(self, request, **kw):
        if isinstance(request, getPlatformConfiguration) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(getPlatformConfigurationResponse.typecode)
        return response

    # op: queryPeers
    def queryPeers(self, request, **kw):
        if isinstance(request, queryPeers) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryPeersResponse.typecode)
        return response

    # op: queryVotes
    def queryVotes(self, request, **kw):
        if isinstance(request, queryVotes) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryVotesResponse.typecode)
        return response

    # op: getAuStatus
    def getAuStatus(self, request, **kw):
        if isinstance(request, getAuStatus) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(getAuStatusResponse.typecode)
        return response

    # op: getAuIds
    def getAuIds(self, request, **kw):
        if isinstance(request, getAuIds) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(getAuIdsResponse.typecode)
        return response

    # op: queryPlugins
    def queryPlugins(self, request, **kw):
        if isinstance(request, queryPlugins) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryPluginsResponse.typecode)
        return response

    # op: queryAus
    def queryAus(self, request, **kw):
        if isinstance(request, queryAus) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryAusResponse.typecode)
        return response

    # op: queryPolls
    def queryPolls(self, request, **kw):
        if isinstance(request, queryPolls) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryPollsResponse.typecode)
        return response

    # op: isDaemonReady
    def isDaemonReady(self, request, **kw):
        if isinstance(request, isDaemonReady) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(isDaemonReadyResponse.typecode)
        return response

    # op: queryTdbTitles
    def queryTdbTitles(self, request, **kw):
        if isinstance(request, queryTdbTitles) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryTdbTitlesResponse.typecode)
        return response

    # op: queryRepositories
    def queryRepositories(self, request, **kw):
        if isinstance(request, queryRepositories) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryRepositoriesResponse.typecode)
        return response

    # op: queryRepositorySpaces
    def queryRepositorySpaces(self, request, **kw):
        if isinstance(request, queryRepositorySpaces) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryRepositorySpacesResponse.typecode)
        return response

    # op: queryTdbPublishers
    def queryTdbPublishers(self, request, **kw):
        if isinstance(request, queryTdbPublishers) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryTdbPublishersResponse.typecode)
        return response

    # op: queryCrawls
    def queryCrawls(self, request, **kw):
        if isinstance(request, queryCrawls) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(queryCrawlsResponse.typecode)
        return response

    # op: getAuUrls
    def getAuUrls(self, request, **kw):
        if isinstance(request, getAuUrls) is False:
            raise TypeError, "%s incorrect request type" % (request.__class__)
        # no input wsaction
        self.binding.Send(None, None, request, soapaction="", **kw)
        # no output wsaction
        response = self.binding.Receive(getAuUrlsResponse.typecode)
        return response

queryTdbAus = GED("http://status.ws.lockss.org/", "queryTdbAus").pyclass

queryTdbAusResponse = GED("http://status.ws.lockss.org/", "queryTdbAusResponse").pyclass

getPlatformConfiguration = GED("http://status.ws.lockss.org/", "getPlatformConfiguration").pyclass

getPlatformConfigurationResponse = GED("http://status.ws.lockss.org/", "getPlatformConfigurationResponse").pyclass

queryPeers = GED("http://status.ws.lockss.org/", "queryPeers").pyclass

queryPeersResponse = GED("http://status.ws.lockss.org/", "queryPeersResponse").pyclass

queryVotes = GED("http://status.ws.lockss.org/", "queryVotes").pyclass

queryVotesResponse = GED("http://status.ws.lockss.org/", "queryVotesResponse").pyclass

getAuStatus = GED("http://status.ws.lockss.org/", "getAuStatus").pyclass

getAuStatusResponse = GED("http://status.ws.lockss.org/", "getAuStatusResponse").pyclass

getAuIds = GED("http://status.ws.lockss.org/", "getAuIds").pyclass

getAuIdsResponse = GED("http://status.ws.lockss.org/", "getAuIdsResponse").pyclass

queryPlugins = GED("http://status.ws.lockss.org/", "queryPlugins").pyclass

queryPluginsResponse = GED("http://status.ws.lockss.org/", "queryPluginsResponse").pyclass

queryAus = GED("http://status.ws.lockss.org/", "queryAus").pyclass

queryAusResponse = GED("http://status.ws.lockss.org/", "queryAusResponse").pyclass

queryPolls = GED("http://status.ws.lockss.org/", "queryPolls").pyclass

queryPollsResponse = GED("http://status.ws.lockss.org/", "queryPollsResponse").pyclass

isDaemonReady = GED("http://status.ws.lockss.org/", "isDaemonReady").pyclass

isDaemonReadyResponse = GED("http://status.ws.lockss.org/", "isDaemonReadyResponse").pyclass

queryTdbTitles = GED("http://status.ws.lockss.org/", "queryTdbTitles").pyclass

queryTdbTitlesResponse = GED("http://status.ws.lockss.org/", "queryTdbTitlesResponse").pyclass

queryRepositories = GED("http://status.ws.lockss.org/", "queryRepositories").pyclass

queryRepositoriesResponse = GED("http://status.ws.lockss.org/", "queryRepositoriesResponse").pyclass

queryRepositorySpaces = GED("http://status.ws.lockss.org/", "queryRepositorySpaces").pyclass

queryRepositorySpacesResponse = GED("http://status.ws.lockss.org/", "queryRepositorySpacesResponse").pyclass

queryTdbPublishers = GED("http://status.ws.lockss.org/", "queryTdbPublishers").pyclass

queryTdbPublishersResponse = GED("http://status.ws.lockss.org/", "queryTdbPublishersResponse").pyclass

queryCrawls = GED("http://status.ws.lockss.org/", "queryCrawls").pyclass

queryCrawlsResponse = GED("http://status.ws.lockss.org/", "queryCrawlsResponse").pyclass

getAuUrls = GED("http://status.ws.lockss.org/", "getAuUrls").pyclass

getAuUrlsResponse = GED("http://status.ws.lockss.org/", "getAuUrlsResponse").pyclass
