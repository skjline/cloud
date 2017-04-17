package com.challenge.ct.mbs.aws;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.challenge.ct.mbs.object.Address;
import com.challenge.ct.mbs.object.User;
import com.challenge.ct.mbs.utils.Const;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * A DynamoDB client interface
 */

public class ContactDatabase {
    private AmazonDynamoDBClient client;

    private String status = Const.INITIALIZE;

    private Emitter<String> dbStateChangeEmitter;
    private Observable<String> onDBStateChanged = Observable.create(new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(ObservableEmitter<String> emitter) throws Exception {
            dbStateChangeEmitter = emitter;
        }
    });

    // holds the latest users and addresses
    // todo: reevaluate, these may not be necessary
    private Map<String, User> users;
    private Map<String, Address> addresses;

    public boolean isActive() {
        return client != null && status.toLowerCase().equals(Const.CONNECTED);
    }

    public Observable<String> getOnDBStateChange() {
        return onDBStateChanged;
    }

    public void initializeDatabase(Cognito cognito) {
        if (cognito == null || !cognito.isAuthorized()) {
            Log.e("Contact DB", "Unauthorized user");
            return;
        }

        client = new AmazonDynamoDBClient(cognito.getProvider());
        client.setRegion(Region.getRegion(Regions.US_EAST_1));

        if (getTableStatus(Const.TABLE_NAME_ADDRESS).toLowerCase().equals(Const.CONNECTED)) {
            initializeLocalTable();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (dbStateChangeEmitter != null) {
            dbStateChangeEmitter.onComplete();
        }

        super.finalize();
    }

    /**
     * Retrieves the table description and returns the table status as a string.
     */
    public String getTableStatus(String tableName) {
        if (client == null) {
            throw new IllegalStateException("Database isn't initialized");
        }

        DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
        DescribeTableResult result;

        try {
            result = client.describeTable(request);
        } catch (AmazonServiceException ex) {
            ex.printStackTrace();
            return Const.ERROR;
        }

        String state = result.getTable().getTableStatus();
        updateDatabaseStatus(TextUtils.isEmpty(state) ? Const.DISCONNECT : state);

        return status;
    }

    public Address getAddressWithId(String addressId) {
        return (TextUtils.isEmpty(addressId) || addresses == null) ?
                null : addresses.get(addressId);
    }

    public User getUserWithId(String userId) {
        return (TextUtils.isEmpty(userId) || users == null) ?
                null : users.get(userId);
    }

    /**
     * Scans the table and returns the list of address.
     */
    public Single<List<Address>> getAddresses() {
        if (client == null) {
            return null;
        }

        return fetchListDataFromDB(Address.class, client, new DynamoDBScanExpression())
                // keep the latest addresses
                .map(list -> {
                    addresses = new HashMap<>();
                    for (Address address : list) {
                        addresses.put(address.getAddressId(), address);
                    }

                    return list;
                });
    }

    /**
     * Scans the table and returns the list of contact users.
     * A convenience method for non-filtered database scan
     */
    public Single<List<User>> getUsers() {
        return getUsers(null);
    }

    /**
     * Scans the table and returns the list of contact users.
     *
     * @param query Partial string to search in both first and last name
     */
    public Single<List<User>> getUsers(String query) {
        if (client == null) {
            return null;
        }

        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        if (!TextUtils.isEmpty(query)) {
            // Add condition for partial name search
            final Condition search = new Condition()
                    .withComparisonOperator(ComparisonOperator.CONTAINS)
                    .withAttributeValueList(new AttributeValue(query));
            scanExpression.addFilterCondition("lastName", search);
            scanExpression.addFilterCondition("firstName", search);
            scanExpression.withConditionalOperator(ConditionalOperator.OR);
        }

        return fetchListDataFromDB(User.class, client, scanExpression)
                // keep the latest users
                .map(list -> {
                    users = new HashMap<>();
                    for (User user : list) {
                        users.put(user.getUserId(), user);
                    }

                    // sort list by last name attribute
                    Collections.sort(list, (c1, c2) -> c1.getLastName().compareTo(c2.getLastName()));
                    return list;
                });
    }

    /**
     * Inserts and/or Update Address to DynamoDB table
     *
     * @param address address to insert or update
     * @return Successfully inserted address when succeeds
     * @throws AmazonServiceException when fails to insert or update an address record
     */
    public Single<String> createOrUpdateAddress(@NonNull final Address address) {
        return executeCallableDatabaseAction(() -> {
            new DynamoDBMapper(client).save(address);
            return address.getAddressId();
        }).onErrorReturn(throwable -> {
            throwable.printStackTrace();
            Log.e("Error AddressTable", throwable.getMessage());

            return "";
        });
    }


    /**
     * Inserts and/or Update User to DynamoDB table
     *
     * @param user contact user to insert or update
     */
    public Single<String> createOrUpdateUser(@NonNull final User user) {
        return executeCallableDatabaseAction(() -> {
            new DynamoDBMapper(client).save(user);
            return user.getUserId();
        }).onErrorReturn(throwable -> {
            throwable.printStackTrace();
            Log.e("Error UserTable", throwable.getMessage());

            return "";
        });
    }

    public Single<String> removeUser(@NonNull final User user) {
        return executeCallableDatabaseAction(() -> {
            DynamoDBMapper mapper = new DynamoDBMapper(client);

            Address address = getAddressWithId(user.getAddressId());
            if (address != null) {
                mapper.delete(address);
            }
            mapper.delete(user);

            return user.getUserId();
        });
    }

    private void updateDatabaseStatus(String state) {
        if (dbStateChangeEmitter == null || status.equals(state)) {
            return;
        }

        status = state;

        dbStateChangeEmitter.onNext(state);
    }

    private void initializeLocalTable() {
        // initialize the data map
        getUsers().subscribe(list ->
                Log.i("ContactDatabase", "Contacts available: " +
                        String.valueOf(list == null ? 0 : list.size()))
        );
        getAddresses().subscribe(list ->
                Log.i("ContactDatabase", "Addresses available: " +
                        String.valueOf(list == null ? 0 : list.size()))
        );
    }

    private <T> Single<List<T>> fetchListDataFromDB(Class<T> type,
                                                    final AmazonDynamoDBClient client,
                                                    final DynamoDBScanExpression scanExpression) {
        final DynamoDBMapper mapper = new DynamoDBMapper(client);

        return Single.fromCallable(() -> mapper.scan(type, scanExpression))
                .subscribeOn(Schedulers.io())
                .map(scanned -> {
                    // Convert PaginatedScanList<T> to List<T>
                    List<T> result = new ArrayList<>();

                    if (scanned != null) {
                        result.addAll(scanned);
                    }

                    return result;
                })
                .onErrorReturn(throwable -> {
                    Log.e("ContactDatabase", throwable.getMessage());
                    return new ArrayList<>();
                });
    }

    private Single<String> executeCallableDatabaseAction(Callable<String> callable) {
        if (client == null) {
            return Single.just("");
        }

        return Single.fromCallable(callable)
                .subscribeOn(Schedulers.io())
                .map(itemId -> {
                    if (dbStateChangeEmitter != null) {
                        dbStateChangeEmitter.onNext(Const.UPDATE);
                    }
                    return itemId;
                });
    }
}
