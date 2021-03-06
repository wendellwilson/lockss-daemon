/*
 * $Id$
 */

/*

Copyright (c) 2000-2005 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Stanford University shall not
be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from Stanford University.

*/

package org.lockss.protocol;

import java.io.*;
import java.util.*;

import org.apache.commons.collections.Predicate;

import org.lockss.app.*;
import org.lockss.config.Configuration;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.util.*;
import org.lockss.hasher.*;

/**
 * <p>Abstraction for identity of a LOCKSS cache. Currently wraps an
 * IP address.<p>
 * @author Claire Griffin
 * @version 1.0
 */
public interface IdentityManager extends LockssManager {
  /**
   * <p>A prefix common to all parameters defined by this class.</p>
   */
  public static final String PREFIX = Configuration.PREFIX + "id.";

  /**
   * <p>The LOCAL_IP parameter.</p>
   */
  public static final String PARAM_LOCAL_IP =
    Configuration.PREFIX + "localIPAddress";

  /**
   * <p>The TCP port for the local V3 identity
   * (at org.lockss.localIPAddress). Can be overridden by
   * org.lockss.platform.v3.port.</p>
   */
  public static final String PARAM_LOCAL_V3_PORT =
    Configuration.PREFIX + "localV3Port";

  /**
   * <p>Local V3 identity string. If this is set it will take
   * precedence over org.lockss.platform.v3.identity.</p> */
  public static final String PARAM_LOCAL_V3_IDENTITY =
    Configuration.PREFIX + "localV3Identity";

  /**
   * <p>If true, restored agreement maps will be merged with any
   * already-loaded map
   */
  public static final String PARAM_MERGE_RESTORED_AGREE_MAP =
    Configuration.PREFIX + "id.mergeAgreeMap";

  /**
   * <p>The default value for the MERGE_RESTORED_AGREE_MAP
   * parameter.</p>
   */
  public static final boolean DEFAULT_MERGE_RESTORED_AGREE_MAP = true;

  /**
   * <p>The IDDB_DIR parameter.</p>
   */
  public static final String PARAM_IDDB_DIR = PREFIX + "database.dir";

  /**
   * <p>The name of the pre-V3 IDDB file.</p>
   */
  public static final String V1_IDDB_FILENAME = "iddb.xml";

  /**
   * <p>If true, any old (V1) IDDB files will be deleted on startup.</p>
   */
  public static final String PARAM_DELETE_OLD_IDDB_FILES =
    PREFIX + "deleteOldIddbFiles";
  public static final boolean DEFAULT_DELETE_OLD_IDDB_FILES = false;

  /**
   * <p>The name of the IDDB file.</p>
   */
  public static final String IDDB_FILENAME = "iddb_v3.xml";

  /**
   * <p>The mapping file for this class.</p>
   */
  public static final String MAPPING_FILE_NAME =
    "/org/lockss/protocol/idmapping.xml";
  // CASTOR: Remove the field above when Castor is phased out.

  /** The minimum percent agreement required before we are
   * willing to serve a repair to a peer.
   */
  public static final String PARAM_MIN_PERCENT_AGREEMENT =
    PREFIX + "minPercentAgreement";

  /** The default percent agreement required to signal agreement
   * with a peer.
   */
  public static final float DEFAULT_MIN_PERCENT_AGREEMENT =
    0.5f;

  /**
   * <p>The MAX_DELTA reputation constant.</p>
   */
  public static final int MAX_DELTA = 0;

  /**
   * <p>The MAX_DELTA reputation constant.</p>
   */
  public static final int AGREE_VOTE = 1;

  /**
   * <p>The DISAGREE_VOTE reputation constant.</p>
   */
  public static final int DISAGREE_VOTE = 2;

  /**
   * <p>The CALL_INTERNAL reputation constant.</p>
   */
  public static final int CALL_INTERNAL = 3;

  /**
   * <p>The SPOOF_DETECTED reputation constant.</p>
   */
  public static final int SPOOF_DETECTED = 4;

  /**
   * <p>The REPLAY_DETECTED reputation constant.</p>
   */
  public static final int REPLAY_DETECTED = 5;

  /**
   * <p>The ATTACK_DETECTED reputation constant.</p>
   */
  public static final int ATTACK_DETECTED = 6;

  /**
   * <p>The VOTE_NOTVERIFIED reputation constant.</p>
   */
  public static final int VOTE_NOTVERIFIED = 7;

  /**
   * <p>The VOTE_VERIFIED reputation constant.</p>
   */
  public static final int VOTE_VERIFIED = 8;

  /**
   * <p>The VOTE_DISOWNED reputation constant.</p>
   */
  public static final int VOTE_DISOWNED = 9;

  /**
   * <p>Currently the only allowed V3 protocol.</p>
   */
  public static final String V3_ID_PROTOCOL_TCP = "TCP";

  /**
   * <p>The V3 protocol separator.</p>
   */
  public static final String V3_ID_PROTOCOL_SUFFIX = ":";

  /**
   * <p>The V3 TCP IP addr prefix.</p>
   */
  public static final String V3_ID_TCP_ADDR_PREFIX = "[";

  /**
   * <p>The V3 TCP IP addr suffix.</p>
   */
  public static final String V3_ID_TCP_ADDR_SUFFIX = "]";

  /**
   * <p>The V3 TCP IP / port separator.</p>
   */
  public static final String V3_ID_TCP_IP_PORT_SEPARATOR = ":";

  /**
   * <p>The initial reputation value.</p>
   */
  public static final int INITIAL_REPUTATION = 500;

  /**
   * <p>Finds or creates unique instances of PeerIdentity.</p>
   */
  public PeerIdentity findPeerIdentity(String key)
      throws MalformedIdentityKeyException;

  /**
   * <p>Finds or creates unique instances of LcapIdentity.</p>
   */
  public LcapIdentity findLcapIdentity(PeerIdentity pid, String key)
      throws MalformedIdentityKeyException;

  /**
   * <p>Returns the peer identity matching the IP address and port;
   * An instance is created if necesary.</p>
   * <p>Used only by LcapDatagramRouter (and soon by its stream
   * analog).</p>
   * @param addr The IPAddr of the peer, null for the local peer.
   * @param port The port of the peer.
   * @return The PeerIdentity representing the peer.
   */
  public PeerIdentity ipAddrToPeerIdentity(IPAddr addr, int port)
      throws MalformedIdentityKeyException;

  public PeerIdentity ipAddrToPeerIdentity(IPAddr addr)
      throws MalformedIdentityKeyException;


  /**
   * <p>Returns the peer identity matching the String IP address and
   * port. An instance is created if necesary. Used only by
   * LcapMessage (and soon by its stream analog).
   * @param idKey the ip addr and port of the peer, null for the local
   *              peer.
   * @return The PeerIdentity representing the peer.
   */
  public PeerIdentity stringToPeerIdentity(String idKey)
      throws IdentityManager.MalformedIdentityKeyException;

  public IPAddr identityToIPAddr(PeerIdentity pid);

  /**
   * <p>Returns the local peer identity.</p>
   * @param pollVersion The poll protocol version.
   * @return The local peer identity associated with the poll version.
   * @throws IllegalArgumentException if the pollVersion is not
   *                                  configured or is outside the
   *                                  legal range.
   */
  public PeerIdentity getLocalPeerIdentity(int pollVersion);

  /**
   * @return a list of all local peer identities.
   */
  public List<PeerIdentity> getLocalPeerIdentities();

  /**
   * <p>Returns the IPAddr of the local peer.</p>
   * @return The IPAddr of the local peer.
   */
  public IPAddr getLocalIPAddr();

  /**
   * <p>Determines if this PeerIdentity is the same as the local
   * host.</p>
   * @param id The PeerIdentity.
   * @return true if is the local identity, false otherwise.
   */
  public boolean isLocalIdentity(PeerIdentity id);

  /**
   * <p>Determines if this PeerIdentity is the same as the local
   * host.</p>
   * @param idStr The string representation of the voter's
   *        PeerIdentity.
   * @return true if is the local identity, false otherwise.
   */
  public boolean isLocalIdentity(String idStr);

  /**
   * <p>Associates the event with the peer identity.</p>
   * @param id    The PeerIdentity.
   * @param event The event code.
   * @param msg   The LcapMessage involved.
   */
  public void rememberEvent(PeerIdentity id, int event, LcapMessage msg);

  /**
   * <p>Returns the max value of an Identity's reputation.</p>
   * @return The int value of max reputation.
   */
  public int getMaxReputation();

  /**
   * <p>Returns the reputation of the peer.</p>
   * @param id The PeerIdentity.
   * @return The peer's reputation.
   */
  public int getReputation(PeerIdentity id);

  /**
   * <p>Makes the change to the reputation of the peer "id" matching
   * the event "changeKind".
   * @param id         The PeerIdentity of the peer to affect.
   * @param changeKind The type of event that is being reflected.
   */
  public void changeReputation(PeerIdentity id, int changeKind);

  /**
   * <p>Used by the PollManager to record the result of tallying a
   * poll.</p>
   * @see #storeIdentities(ObjectSerializer)
   */
  public void storeIdentities() throws ProtocolException;

  /**
   * <p>Records the result of tallying a poll using the given
   * serializer.</p>
   */
  public void storeIdentities(ObjectSerializer serializer)
      throws ProtocolException;

  /**
   * <p>Copies the identity database file to the stream.</p>
   * @param out An OutputStream instance.
   */
  public void writeIdentityDbTo(OutputStream out) throws IOException;

  /**
   * Deprecated as of daemon 1.25.  There are currently no callers of this
   * method.  Please remove after a few daemon releases.
   * 
   * @deprecated
   */
  public IdentityListBean getIdentityListBean();

  /**
   * Return a list of all known UDP (suitable for V1) peer identities.
   */
  public Collection getUdpPeerIdentities();

  /**
   * Return a list of all known TCP (V3) peer identities.
   */
  public Collection getTcpPeerIdentities();

  /**
   * Return a filtered list of all known TCP (V3) peer identities.
   */
  public Collection getTcpPeerIdentities(Predicate peerPredicate);

  /**
   * <p>Signals that we've agreed with pid on a top level poll on
   * au.</p>
   * <p>Only called if we're both on the winning side.</p>
   * @param pid The PeerIdentity of the agreeing peer.
   * @param au  The {@link ArchivalUnit}.
   */
  public void signalAgreed(PeerIdentity pid, ArchivalUnit au);

  /**
   * <p>Signals that we've disagreed with pid on any level poll on
   * au.</p>
   * <p>Only called if we're on the winning side.</p>
   * @param pid The PeerIdentity of the disagreeing peer.
   * @param au  The {@link ArchivalUnit}.
   */
  public void signalDisagreed(PeerIdentity pid, ArchivalUnit au);

  /**
   * Signal partial agreement with a peer on a given archival unit following
   * a V3 poll at the poller.
   * 
   * @param pid  The PeerIdentity of the agreeing peer.
   * @param au  The {@link ArchivalUnit}.
   * @param agreement  A number between 0.0 and 1.0 representing the percentage
   *                   of agreement on the total AU.
   */
  public void signalPartialAgreement(PeerIdentity pid, ArchivalUnit au,
                                     float agreement);
  
  /**
   * Signal partial agreement with a peer on a given archival unit following
   * a V3 poll at the voter based on the hint in the receipt.
   * 
   * @param pid  The PeerIdentity of the agreeing peer.
   * @param au  The {@link ArchivalUnit}.
   * @param agreement  A number between 0.0 and 1.0 representing the percentage
   *                   of agreement on the total AU.
   */
  public void signalPartialAgreementHint(PeerIdentity pid, ArchivalUnit au,
                                         float agreement);

  /**
   * Signal partial agreement with a peer on a given archival unit following
   * a V3 poll.
   *
   * @param agreementType The {@link AgreementType} to be recorded.
   * @param pid The {@link PeerIdentity} of the agreeing peer.
   * @param au The {@link ArchivalUnit}.
   * @param agreement A number between {@code 0.0} and {@code 1.0}
   *                   representing the percentage of agreement on the
   *                   portion of the AU polled.
   */
  public void signalPartialAgreement(AgreementType agreementType, 
				     PeerIdentity pid, ArchivalUnit au,
                                     float agreement);

  /**
   * Signal the completion of a local hash check.
   *
   * @param filesCount The number of files checked.
   * @param urlCount The number of URLs checked.
   * @param agreeCount The number of files which agreed with their
   * previous hash value.
   * @param disagreeCount The number of files which disagreed with
   * their previous hash value.
   * @param missingCount The number of files which had no previous
   * hash value.
   */
  public void signalLocalHashComplete(LocalHashResult lhr);
  
  /**
   * Return the percent agreement for a given peer on a given
   * {@link ArchivalUnit}.  Used only by V3 Polls.
   * 
   * @param pid The {@link PeerIdentity}.
   * @param au The {@link ArchivalUnit}.
   * @return The percent agreement for the peer on the au.
   */
  public float getPercentAgreement(PeerIdentity pid, ArchivalUnit au);
  
  /** Return the highest percent agreement recorded for the given peer
   * on a given {@link ArchivalUnit}.
   * 
   * @param pid The {@link PeerIdentity}.
   * @param au The {@link ArchivalUnit}.
   * @return The highest percent agreement for the peer on the au.
   */
  public float getHighestPercentAgreement(PeerIdentity pid, ArchivalUnit au);
  
  /** Return agreement peer has most recently seen from us.
   * @param pid The {@link PeerIdentity}.
   * @param au The {@link ArchivalUnit}.
   * @return agreement, -1.0 if not known */
  public float getPercentAgreementHint(PeerIdentity pid, ArchivalUnit au);
  
  /** Return highest agreement peer has seen from us.
   * @param pid The {@link PeerIdentity}.
   * @param au The {@link ArchivalUnit}.
   * @return agreement, -1.0 if not known */
  public float getHighestPercentAgreementHint(PeerIdentity pid,
					      ArchivalUnit au);

  /**
   * A list of peers with whom we have had a POR poll and a result
   * above the minimum threshold for repair.
   *
   * NOTE: No particular order should be assumed.
   * NOTE: This does NOT use the "hint", which would be more reasonable.
   *
   * @param au ArchivalUnit to look up PeerIdentities for.
   * @return List of peers from which to try to fetch repairs for the
   *         AU.
   */
  public List<PeerIdentity> getCachesToRepairFrom(ArchivalUnit au);

  /**
   * Count the peers with whom we have had a POR poll and a result
   * above the minimum threshold for repair.
   *
   * NOTE: This does NOT use the "hint", which would be more reasonable.
   *
   * @param au ArchivalUnit to look up PeerIdentities for.
   * @return Count of peers we believe are willing to send us repairs for
   * this AU.
   */
  public int countCachesToRepairFrom(ArchivalUnit au);

  /**
   * Return a mapping for each peer for which we have an agreement of
   * the requested type, to the {@link PeerAgreement} record for that
   * peer.
   *
   * @param au The {@link ArchivalUnit} in question.
   * @param type The {@link AgreementType} to look for.
   * @return A Map mapping each {@link PeerIdentity} which has an
   * agreement of the requested type to the {@link PeerAgreement} for
   * that type.
   */
  public Map<PeerIdentity, PeerAgreement> getAgreements(ArchivalUnit au,
							AgreementType type);

  public boolean hasAgreed(String ip, ArchivalUnit au)
      throws MalformedIdentityKeyException;

  public boolean hasAgreed(PeerIdentity pid, ArchivalUnit au);

  /** Convenience methods returns agreement on AU au, of AgreementType type
   * with peer pid */
  public float getPercentAgreement(PeerIdentity pid,
				   ArchivalUnit au,
				   AgreementType type);

  /** Convenience methods returns agreement on AU au, of AgreementType type
   * with peer pid */
  public float getHighestPercentAgreement(PeerIdentity pid,
					  ArchivalUnit au,
					  AgreementType type);

  /**
   * <p>Return map peer -> last agree time. Used for logging and
   * debugging.</p>
   */
  public Map getAgreed(ArchivalUnit au);

  /**
   * @return {@code true} iff there are no data on agreements.
   */
  public boolean hasAgreeMap(ArchivalUnit au);
  
  /**
   * <p>Remove a peer from our list of known peers.
   */
  public void removePeer(String key);

  /**
   * <p>Copies the identity agreement file for the AU to the given
   * stream.</p>
   * @param au  An archival unit.
   * @param out An output stream.
   * @throws IOException if input or output fails.
   */
  public void writeIdentityAgreementTo(ArchivalUnit au, OutputStream out)
      throws IOException;

  /**
   * <p>Installs the contents of the stream as the identity agreement
   * file for the AU.</p>
   * @param au An archival unit.
   * @param in An input stream to read from.
   */
  public void readIdentityAgreementFrom(ArchivalUnit au, InputStream in)
      throws IOException;
  
  /**
   * @return List of  PeerIdentityStatus for all PeerIdentity.
   */
  public List<PeerIdentityStatus> getPeerIdentityStatusList();

  /**
   * @param pid The PeerIdentity.
   * @return The PeerIdentityStatus associated with the given PeerIdentity.
   */
  public PeerIdentityStatus getPeerIdentityStatus(PeerIdentity pid);
  
  /**
   * @param key The Identity Key
   * @return The PeerIdentityStatus associated with the given PeerIdentity.
   */
  public PeerIdentityStatus getPeerIdentityStatus(String key);

  public String getUiUrlStem(PeerIdentity pid);

  public static class IdentityAgreement implements LockssSerializable {
    private long lastAgree = 0;
    private long lastDisagree = 0;
    // The MOST RECENT percent agreement we tallied for a vote from this
    // peer in a poll we called.
    private float percentAgreement = 0.0f;
    // The highest agreement we have EVER tallied in a vote from this peer.
    private float highestPercentAgreement = 0.0f;
    // The MOST RECENT percent agreement this peer reported in the receipt
    // for one of our votes in a poll this peer called.
    private float percentAgreementHint = -1.0f;
    // The highest percent agreement we have ever seen in a receipt from
    // one of any of our votes in polls this peer called.
    private float highestPercentAgreementHint = -1.0f;
    // Hists were added later; deserializing stored state with no hints
    // sets them to zero (constructor doesn't run) so can't tell if they
    // were ever really set.  This flag serves that function.
    private boolean haveHints = false;

    private String id = null;

    IdentityAgreement(String id) {
      this.id = id;
    }

    public IdentityAgreement(PeerIdentity pid) {
      this(pid.getIdString());
    }

    // needed for marshalling
    public IdentityAgreement() {}

    public long getLastAgree() {
      return lastAgree;
    }

    public void setLastAgree(long lastAgree) {
      this.lastAgree = lastAgree;
    }

    public long getLastDisagree() {
      return lastDisagree;
    }

    public void setLastDisagree(long lastDisagree) {
      this.lastDisagree = lastDisagree;
    }
    
    public long getLastSignalTime() {
      return lastAgree > lastDisagree ? lastAgree : lastDisagree;
    }

    public float getHighestPercentAgreement() {
      return agreePercentValue(highestPercentAgreement);
    }
    
    public void setHighestPercentAgreement(float agreement) {
      this.highestPercentAgreement = agreement;
    }
    
    public float getPercentAgreement() {
      return agreePercentValue(percentAgreement);
    }
    
    public void setPercentAgreement(float percentAgreement) {
      this.percentAgreement = percentAgreement;
      if (percentAgreement > highestPercentAgreement) {
        setHighestPercentAgreement(percentAgreement);
      }
    }

    /** Return highest agreement peer has seen from us.
     * @return highest agreement, -1.0 if not known */
    public float getHighestPercentAgreementHint() {
      return hintValue(highestPercentAgreementHint);
    }
    
    void setHighestPercentAgreementHint(float agreement) {
      this.highestPercentAgreementHint = agreement;
    }
    
    /** Return agreement peer has most recently seen from us.
     * @return agreement, -1.0 if not known */
    public float getPercentAgreementHint() {
      return hintValue(percentAgreementHint);
    }
    
    public void setPercentAgreementHint(float percentAgreementHint) {
      this.percentAgreementHint = percentAgreementHint;
      if (percentAgreementHint > highestPercentAgreementHint) {
        setHighestPercentAgreementHint(percentAgreementHint);
      }
      haveHints = true;
    }


    // Assume that if agreee percent is zero and we have no evidence we've
    // set it (), then it isn't really known.
    private float agreePercentValue(float agree) {
      if (lastAgree > 0 || lastDisagree > 0 || agree != 0.0) {
	return agree;
      } else {
	return -1.0f;
      }
    }

    // Assume that if hint is zero and we have no evidence we've set it
    // (haveHints), then it isn't really known.
    private float hintValue(float hint) {
      if (haveHints || hint != 0.0) {
	return hint;
      } else {
	return -1.0f;
      }
    }


    public String getId() {
      return id;
    }

    public boolean hasAgreed() {
      return lastAgree != 0;
    }

    public void setId(String id) {
      this.id = id;
    }

    public void mergeFrom(IdentityAgreement ida) {
      long ag = ida.getLastAgree();
      if (ag > getLastAgree()) {
        setLastAgree(ag);
      }
      long dis = ida.getLastDisagree();
      if (dis > getLastDisagree()) {
        setLastDisagree(dis);
      }
    }

    /** 
     * The highest percent agreement may need to be initialized to the
     * most recent agreement level if this is the first time the agreement
     * has been loaded since the highestPercentAgreement field was added.
     */
    protected void postUnmarshal(LockssApp lockssContext) {
      if (highestPercentAgreement < percentAgreement) {
        highestPercentAgreement = percentAgreement;
      }
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[IdentityAgreement: ");
      sb.append("id=");
      sb.append(id);
      sb.append(", lastAgree=");
      sb.append(lastAgree);
      sb.append(", lastDisagree=");
      sb.append(lastDisagree);
      sb.append(", (agree,max)=(");
      sb.append(percentAgreement);
      sb.append(",");
      sb.append(highestPercentAgreement);
      sb.append(", hints=");
      sb.append(haveHints);
      sb.append(", (hint,max)=(");
      sb.append(percentAgreementHint);
      sb.append(",");
      sb.append(highestPercentAgreementHint);
      sb.append("]");
      return sb.toString();
    }

    public boolean equals(Object obj) {
      if (obj instanceof IdentityAgreement) {
        IdentityAgreement ida = (IdentityAgreement)obj;
        return (id.equals(ida.getId())
            && ida.getLastDisagree() == getLastDisagree()
            && ida.getLastAgree() == getLastAgree()
            && ida.getPercentAgreement() == getPercentAgreement()
            && ida.getHighestPercentAgreement() == getHighestPercentAgreement()
            && ida.getPercentAgreementHint() == getPercentAgreementHint()
            && ida.getHighestPercentAgreementHint() == getHighestPercentAgreementHint());
      }
      return false;
    }

    public int hashCode() {
      return 7 * id.hashCode() + 3 * (int)(getLastDisagree() + getLastAgree());
    }
  }

  /**
   * <p>Exception thrown for illegal identity keys.</p>
   */
  public static class MalformedIdentityKeyException extends IOException {
    public MalformedIdentityKeyException(String message) {
      super(message);
    }
  }
}
