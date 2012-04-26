/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.restcomm.dao.mybatis;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import org.joda.time.DateTime;

import org.mobicents.servlet.sip.restcomm.Account;
import org.mobicents.servlet.sip.restcomm.Sid;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.ThreadSafe;
import org.mobicents.servlet.sip.restcomm.dao.AccountsDao;
import static org.mobicents.servlet.sip.restcomm.dao.DaoUtils.*;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe public final class MybatisAccountsDao implements AccountsDao {
  private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.AccountsDao.";
  private final SqlSessionFactory sessions;
  
  public MybatisAccountsDao(final SqlSessionFactory sessions) {
    super();
    this.sessions = sessions;
  }
  
  @Override public void addAccount(final Account account) {
    final SqlSession session = sessions.openSession();
    try {
      session.insert(namespace + "addAccount", toMap(account));
    } finally {
      session.close();
    }
  }
  
  @Override public void addSubAccount(final Sid primaryAccountSid, final Account subAccount) {
	final Map<String, Object> parameters = toMap(subAccount);
	parameters.put("account_sid", primaryAccountSid.toString());
    final SqlSession session = sessions.openSession();
    try {
      session.insert(namespace + "addSubAccount", parameters);
    } finally {
      session.close();
    }
  }

  @Override public Account getAccount(final Sid sid) {
    return getAccount(namespace + "getAccount", sid);
  }
  
  @Override public Account getSubAccount(final Sid sid) {
    return getAccount(namespace + "getSubAccount", sid);
  }
  
  private Account getAccount(final String selector, final Sid sid) {
    final SqlSession session = sessions.openSession();
    try {
	  @SuppressWarnings("unchecked")
      final Map<String, Object> result = (Map<String, Object>)session.selectOne(selector, sid.toString());
      if(result != null) {
        return toAccount(result);
      } else {
        return null;
      }
    } finally {
      session.close();
    }
  }
  
  @Override public List<Account> getSubAccounts(final Sid primaryAccountSid) {
    final SqlSession session = sessions.openSession();
    try {
      @SuppressWarnings("unchecked")
      final List<Map<String, Object>> results = (List<Map<String, Object>>)session.selectList(namespace + "getSubAccounts", primaryAccountSid.toString());
      final List<Account> accounts = new ArrayList<Account>();
      if(results != null && !results.isEmpty()) {
        for(final Map<String, Object> result : results) {
          accounts.add(toAccount(result));
        }
      }
      return accounts;
    } finally {
      session.close();
    }
  }

  @Override public void removeAccount(final Sid sid) {
    removeAccount(namespace + "removeAccount", sid);
  }
  
  @Override public void removeSubAccount(final Sid sid) {
    removeAccount(namespace + "removeSubAccount", sid);
  }
  
  private void removeAccount(final String selector, final Sid sid) {
    final SqlSession session = sessions.openSession();
    try {
      session.delete(selector, sid.toString());
    } finally {
      session.close();
    }
  }

  @Override public void updateAccount(final Account account) {
    updateAccount(namespace + "updateAccount", account);
  }

  @Override public void updateSubAccount(final Account account) {
    updateAccount(namespace + "updateSubAccount", account);
  }
  
  private void updateAccount(final String selector, final Account account) {
    final SqlSession session = sessions.openSession();
    try {
      session.update(selector, toMap(account));
    } finally {
      session.close();
    }
  }
  
  private Account toAccount(final Map<String, Object> map) {
	final Sid sid = readSid(map.get("sid"));
	final DateTime dateCreated = readDateTime(map.get("date_created"));
	final DateTime dateUpdated = readDateTime(map.get("date_updated"));
	final String emailAddress = readString(map.get("email_address"));
	final String friendlyName = readString(map.get("friendly_name"));
	final Account.Type type = readAccountType(map.get("type"));
	final Account.Status status = readAccountStatus(map.get("status"));
	final String authToken = readString(map.get("auth_token"));
	final String role = readString(map.get("role"));
	final URI uri = readUri(map.get("uri"));
    return new Account(sid, dateCreated, dateUpdated, emailAddress, friendlyName, type, status, authToken, role, uri);
  }
  
  private Map<String, Object> toMap(final Account account) {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("sid", writeSid(account.getSid()));
    map.put("date_created", writeDateTime(account.getDateCreated()));
    map.put("date_updated", writeDateTime(account.getDateUpdated()));
    map.put("email_address", account.getEmailAddress());
    map.put("friendly_name", account.getFriendlyName());
    map.put("type", writeAccountType(account.getType()));
    map.put("status", writeAccountStatus(account.getStatus()));
    map.put("auth_token", account.getAuthToken());
    map.put("role", account.getRole());
    map.put("uri", writeUri(account.getUri()));
    return map;
  }
}
