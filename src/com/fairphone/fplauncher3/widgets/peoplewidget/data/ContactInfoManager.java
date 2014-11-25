/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fairphone.fplauncher3.widgets.peoplewidget.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * This class processes the count for the most contacted and the last contacted
 * numbers.
 */
public class ContactInfoManager
{
    public static final boolean HIDE_SECOND_ROW = false;
    public static final int MOST_CONTACTED_MAX_COUNT_LIMIT = 4;
    public static final int LAST_CONTACTED_MAX_COUNT_LIMIT = 7;
    public static final int MINIMAL_COUNT = 2;
    private static final String TAG = null;

    private LimitedQueue<ContactInfo> _mostContacted;
    private LimitedQueue<ContactInfo> _lastContacted;
    private Map<String, ContactInfo> _contactsInfoCache;

    private int _mostContactedLimit;
    private int _lastContactedLimit;

    public ContactInfoManager()
    {

        if (!HIDE_SECOND_ROW)
        {
            setUpLimits(MOST_CONTACTED_MAX_COUNT_LIMIT, LAST_CONTACTED_MAX_COUNT_LIMIT);
        }
        else
        {
            setUpLimits(MOST_CONTACTED_MAX_COUNT_LIMIT / 2, LAST_CONTACTED_MAX_COUNT_LIMIT / 2);
        }

        _contactsInfoCache = new HashMap<String, ContactInfo>();
    }

    public void setUpLimits(int maxMostContacted, int maxLastContacted)
    {
        _mostContactedLimit = maxMostContacted;
        _lastContactedLimit = maxLastContacted;

        // refactor the limits
        setUpNewLimits();
    }

    private void setUpNewLimits()
    {
        _mostContacted = new LimitedQueue<ContactInfo>(_mostContactedLimit);
        _lastContacted = new LimitedQueue<ContactInfo>(_lastContactedLimit);

        // update the information
        if (_contactsInfoCache != null)
        {
            updateContactInformation();
        }
    }

    public void resetState()
    {
        _mostContacted.clear();
        _lastContacted.clear();
        _contactsInfoCache.clear();
    }

    public void contactUsed(ContactInfo contactInfo)
    {
        // obtain the cached contact information
        ContactInfo cachedContact = _contactsInfoCache.get(contactInfo.getPhoneNumberOnE164Format());
        // if does not exist, create one
        if (cachedContact == null)
        {
            _contactsInfoCache.put(contactInfo.getPhoneNumberOnE164Format(), contactInfo);

            cachedContact = contactInfo;

            cachedContact.resetCount();
        }

        // increment count
        cachedContact.incrementCount();

        Log.d(TAG, "Logging contact : " + contactInfo.getPhoneNumberOnE164Format() + " : " + cachedContact.getCount());

        // set the current time for the last execution
        cachedContact.setLastExecution(contactInfo.getLastExecution());
        cachedContact.setLastAction(contactInfo.getLastAction());

        // update the information
        updateContactInformation();
    }

    public void contactRemoved(String phoneNumber)
    {
        // remove data
        ContactInfo contactInfo = _contactsInfoCache.remove(phoneNumber);

        // if does not exist return
        if (contactInfo == null)
        {
            return;
        }

        // if its being used in the lists refactor the lists
        if (_mostContacted.contains(contactInfo) || _lastContacted.contains(contactInfo))
        {
            updateContactInformation();
        }
    }

    private void updateContactInformation()
    {
        _mostContacted.clear();
        _lastContacted.clear();

        // most used
        // calculate the most used
        for (ContactInfo current : _contactsInfoCache.values())
        {

            if (current.getCount() >= MINIMAL_COUNT)
            {
                addByCount(current, _mostContacted, _mostContactedLimit);
            }
        }

        printMostContacted();

        // calculate the most recent
        for (ContactInfo current : _contactsInfoCache.values())
        {
            if (!_mostContacted.contains(current))
            {
                addByDate(current, _lastContacted, _lastContactedLimit);
            }
        }

        printLastContacted();
    }

    private void printLastContacted()
    {
        for (ContactInfo current : _lastContacted)
        {
            Log.d(TAG, "Last contacted - " + current);
        }
    }

    private void printMostContacted()
    {
        for (ContactInfo current : _mostContacted)
        {
            Log.d(TAG, "Most contacted - " + current);
        }
    }

    private static void addByDate(ContactInfo info, LimitedQueue<ContactInfo> queue, int limit)
    {
        for (int insertIdx = 0; insertIdx < queue.size(); insertIdx++)
        {
            if (queue.get(insertIdx).getLastExecution().before(info.getLastExecution()))
            {
                Log.d(TAG, "Qs : " + queue.size() + " : Last contacted : Adding " + info.getPhoneNumberOnE164Format() + " to position " + insertIdx);
                queue.add(insertIdx, info);

                return;
            }
        }

        if (queue.size() < limit)
        {
            queue.addLast(info);
        }
    }

    private static void addByCount(ContactInfo info, LimitedQueue<ContactInfo> queue, int limit)
    {
        for (int insertIdx = 0; insertIdx < queue.size(); insertIdx++)
        {
            if (info.getCount() > queue.get(insertIdx).getCount())
            {
                Log.d(TAG, "Qs : " + queue.size() + " : Most contacted : Adding " + info.getPhoneNumberOnE164Format() + " to position " + insertIdx);
                queue.add(insertIdx, info);

                return;
            }
        }
        if (queue.size() < limit)
        {
            queue.addLast(info);
        }
    }

    private static class LimitedQueue<E> extends LinkedList<E>
    {

        /**
		 * 
		 */
        private static final long serialVersionUID = 8174761694444365605L;
        private final int limit;

        public LimitedQueue(int limit)
        {
            this.limit = limit;
        }

        @Override
        public void add(int idx, E o)
        {
            super.add(idx, o);

            while (size() > limit)
            {
                super.removeLast();
            }
        }

        @Override
        public boolean add(E o)
        {
            super.addLast(o);
            while (size() > limit)
            {
                super.removeLast();
            }
            return true;
        }
    }

    public List<ContactInfo> getLastContacted()
    {

        Log.d(TAG, "Getting Last contacted ... " + _lastContacted.size());
        return _lastContacted;
    }

    public List<ContactInfo> getMostContacted()
    {
        Log.d(TAG, "Getting most contacted... " + _mostContacted.size());

        return _mostContacted;
    }

    public int getMostContactedLimit()
    {
        return _mostContactedLimit;
    }

    public int getLastContactedLimit()
    {
        return _lastContactedLimit;
    }

    public List<ContactInfo> getAllContactInfos()
    {
        return new ArrayList<ContactInfo>(_contactsInfoCache.values());
    }

    public void setAllContactInfo(List<ContactInfo> allApps)
    {
        resetState();

        for (ContactInfo contact : allApps)
        {
            _contactsInfoCache.put(contact.getPhoneNumberOnE164Format(), contact);
        }

        updateContactInformation();
    }

}
