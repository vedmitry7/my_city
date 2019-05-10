package app.mycity.mycity.api;

import com.google.gson.JsonObject;

import app.mycity.mycity.api.model.Album;
import app.mycity.mycity.api.model.CheckTokenResponse;
import app.mycity.mycity.api.model.DialogsContainer;
import app.mycity.mycity.api.model.MessageResponse;
import app.mycity.mycity.api.model.NotificationResponce;
import app.mycity.mycity.api.model.PlaceCategoryResponce;
import app.mycity.mycity.api.model.PlacesResponse;
import app.mycity.mycity.api.model.Profile;
import app.mycity.mycity.api.model.RefreshTokenResponse;
import app.mycity.mycity.api.model.ResponseAddComment;
import app.mycity.mycity.api.model.ResponseAlbums;
import app.mycity.mycity.api.model.ResponseCities;
import app.mycity.mycity.api.model.ResponseComments;
import app.mycity.mycity.api.model.ResponseEventVisitors;
import app.mycity.mycity.api.model.ResponseEvents;
import app.mycity.mycity.api.model.ResponseLike;
import app.mycity.mycity.api.model.PhotoContainer;
import app.mycity.mycity.api.model.ResponseAuth;
import app.mycity.mycity.api.model.ResponseContainer;
import app.mycity.mycity.api.model.ResponsePlaces;
import app.mycity.mycity.api.model.ResponsePostPhoto;
import app.mycity.mycity.api.model.ResponseSavePhoto;
import app.mycity.mycity.api.model.ResponseSaveVideo;
import app.mycity.mycity.api.model.ResponseSocketServer;
import app.mycity.mycity.api.model.ResponseSubcomments;
import app.mycity.mycity.api.model.ResponseUploadServer;
import app.mycity.mycity.api.model.ResponseUploading;
import app.mycity.mycity.api.model.ResponseUploadingVideo;
import app.mycity.mycity.api.model.ResponseVisit;
import app.mycity.mycity.api.model.ResponseWall;
import app.mycity.mycity.api.model.SendMessageResponse;
import app.mycity.mycity.api.model.SettingsResponse;
import app.mycity.mycity.api.model.Success;
import app.mycity.mycity.api.model.SuccessCreatePlace;
import app.mycity.mycity.api.model.SuccessResponceNumber;
import app.mycity.mycity.api.model.UsersContainer;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("auth.authorize")
    Call<JsonObject> auth(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("auth.authorize")
    Call<ResponseContainer<ResponseAuth>> authorize(@Field("email") String email,
                                                    @Field("password") String password);

    @FormUrlEncoded
    @POST("auth.authentication")
    Call<ResponseContainer<CheckTokenResponse>> checkToken(@Field("access_token") String accessToken);

    @FormUrlEncoded
    @POST("auth.refresh")
    Call<ResponseContainer<RefreshTokenResponse>> updateToken(@Field("access_token") String accessToken,
                                                              @Field("refresh_token") String refreshToken);

    @FormUrlEncoded
    @POST("users.get")
    Call<ResponseContainer<Profile>> getUser(@Field("access_token") String accessToken,
                                             @Field("fields") String fields);

    @FormUrlEncoded
    @POST("users.getAll")
    Call<ResponseContainer<UsersContainer>> getTopUsersInPlacesWithSorting(@Field("access_token") String accessToken,
                                                                           @Field("city_id") int cityId,
                                                                           @Field("offset") int offset,
                                                                           @Field("fields") String fields,
                                                                           @Field("order") String order,
                                                                           @Field("filter") String filter, //in_place
                                                                           @Field("q") String search, //in_place
                                                                           @Field("sex") int sex,
                                                                           @Field("age_from") int from,
                                                                           @Field("age_to") int to, @Field("extended") String extended);

    //Subscribers
    @FormUrlEncoded
    @POST("subscribers.get")
    Call<ResponseContainer<UsersContainer>> getSubscribers(@Field("access_token") String accessToken,
                                                           @Field("offset") int offset,
                                                           @Field("user_id") String id,
                                                           @Field("only_online") int online,
                                                           @Field("fields") String fields);

    //Subscribers Count
    @FormUrlEncoded
    @POST("subscribers.get")
    Call<ResponseContainer<UsersContainer>> getSubscribersCount(@Field("access_token") String accessToken);

    //Subscriptions // 1 - online
    @FormUrlEncoded
    @POST("subscribers.getSubscriptions")
    Call<ResponseContainer<UsersContainer>> getSubscriptions(@Field("access_token") String accessToken,
                                                             @Field("offset") int offset,
                                                             @Field("user_id") String id,
                                                             @Field("only_online") int online,
                                                             @Field("fields") String fields);

    @FormUrlEncoded
    @POST("subscribers.getSubscriptions")
    Call<ResponseContainer<UsersContainer>> getSubscriptionsCount(@Field("access_token") String accessToken);


    @FormUrlEncoded
    @POST("users.get")
    Call<ResponseContainer<Profile>> getUserById(@Field("access_token") String accessToken,
                                                 @Field("user_id") String id,
                                                 @Field("fields") String fields);

    @FormUrlEncoded
    @POST("subscribers.add")
    Call<ResponseContainer<Success>> addSubscription(@Field("access_token") String accessToken,
                                                     @Field("user_id") String id);

    @FormUrlEncoded
    @POST("subscribers.delete")
    Call<ResponseContainer<Success>> deleteSubscription(@Field("access_token") String accessToken,
                                                        @Field("user_id") String id);

    @FormUrlEncoded
    @POST("messages.getDialogs")
    Call<ResponseContainer<DialogsContainer>> getDialogs(@Field("access_token") String accessToken,
                                                         @Field("offset") int count);


    @FormUrlEncoded
    @POST("messages.deleteDialog")
    Call<ResponseContainer<SuccessResponceNumber>> deleteDialogs(@Field("access_token") String accessToken,
                                                                 @Field("peer_id") String id);

    //send message
    @FormUrlEncoded
    @POST("messages.send")
    Call<ResponseContainer<SendMessageResponse>> sendMessage(@Field("access_token") String accessToken,
                                                             @Field("peer_id") String user_id,
                                                             @Field("chat_id") long chat_id,
                                                             @Field("text") String text);

    //send message
    @FormUrlEncoded
    @POST("messages.send")
    Call<ResponseContainer<SendMessageResponse>> sendImageMessage(@Field("access_token") String accessToken,
                                                                  @Field("peer_id") String user_id,
                                                                  @Field("chat_id") long chat_id,
                                                                  @Field("attachments") String att);


    @FormUrlEncoded
    @POST("messages.markAsRead")
    Call<ResponseContainer<SuccessResponceNumber>> markAsRead(@Field("access_token") String accessToken,
                                                              @Field("peer_id") String user_id);

    @FormUrlEncoded
    @POST("messages.markAsRead")
    Call<ResponseContainer<SuccessResponceNumber>> markAsReadMessages(@Field("access_token") String accessToken,
                                                                      @Field("message_ids") long message_ids);


    @FormUrlEncoded
    @POST("messages.delete")
    Call<ResponseContainer<SuccessResponceNumber>> deleteMessages(@Field("access_token") String accessToken,
                                                                  @Field("message_ids") long message_ids);

    @FormUrlEncoded
    @POST("messages.getHistory")
    Call<ResponseContainer<MessageResponse>> getMessages(@Field("access_token") String accessToken,
                                                         @Field("peer_id") String peer_id,
                                                         @Field("offset") int offset);

    //UPLOADING

    @FormUrlEncoded
    @POST("photos.getUploadServer")
    Call<ResponseContainer<ResponseUploadServer>> getUploadPhotoServer(@Field("access_token") String accessToken);

    @FormUrlEncoded
    @POST("video.getUploadServer")
    Call<ResponseContainer<ResponseUploadServer>> getUploadVideoServer(@Field("access_token") String accessToken);

    @FormUrlEncoded
    @POST("photos.getUploadServerAvatar")
    Call<ResponseContainer<ResponseUploadServer>> getUploadServerAvatar(@Field("access_token") String accessToken);

    @Multipart
    @POST("upload.php")
    Call<ResponseContainer<ResponseUploading>> uploadPhoto(@Part("action") RequestBody action,
                                                           @Part("user_id") RequestBody userId,
                                                           @Part MultipartBody.Part filePart);
    @Multipart
    @POST("upload_video.php")
    Call<ResponseContainer<ResponseUploadingVideo>> uploadVideo(@Part("action") RequestBody action,
                                                                @Part("user_id") RequestBody userId,
                                                                @Part MultipartBody.Part filePart);

    @FormUrlEncoded
    @POST("photos.saveUserPhoto")
    Call<ResponseContainer<ResponseSavePhoto>> savePhoto(@Field("access_token") String accessToken,
                                                         @Field("photo_list") String photoList,
                                                         @Field("server") String server);

    @FormUrlEncoded
    @POST("photos.save")
    Call<ResponseContainer<ResponseSavePhoto>> savePhoto(@Field("access_token") String accessToken,
                                                         @Field("photo_list") String photoList,
                                                         @Field("album_id") String albumId,
                                                         @Field("server") String server);

    @FormUrlEncoded
    @POST("video.save")
    Call<ResponseContainer<ResponseSaveVideo>> saveVideo(@Field("access_token") String accessToken,
                                                         @Field("video_list") String videoList,
                                                         @Field("server") String server);

    @FormUrlEncoded
    @POST("photos.getAlbums")
    Call<ResponseContainer<ResponseAlbums>> getGroupAlbums(@Field("access_token") String accessToken,
                                                           @Field("group_id") String groupId,
                                                           @Field("offset") int offset);

    @FormUrlEncoded
    @POST("photos.getAlbums")
    Call<ResponseContainer<ResponseAlbums>> getAllGroupAlbums(@Field("access_token") String accessToken,
                                                              @Field("city_id") int cityId,
                                                              @Field("q") String search,
                                                              @Field("offset") int offset,
                                                              @Field("extended") String extended,
                                                              @Field("only_subscription") String subscriptionOnly);

    @FormUrlEncoded
    @POST("photos.getAll")
    Call<ResponseContainer<PhotoContainer>> getAlbum(@Field("access_token") String accessToken,
                                                     @Field("group_id") String groupId,
                                                     @Field("album_id") String albumId,
                                                     @Field("extended") String extended);
    @FormUrlEncoded
    @POST("photos.getAlbumById")
    Call<ResponseContainer<Album>> getAlbumById(@Field("access_token") String accessToken,
                                                @Field("album_id") String groupId);

    @FormUrlEncoded
    @POST("photos.getAllInAlbumByPhotoId")
    Call<ResponseContainer<PhotoContainer>> getAlbumByPhotoId(@Field("access_token") String accessToken,
                                                              @Field("photo_id") String photoId,
                                                              @Field("extended") String extended);

    @FormUrlEncoded
    @POST("account.removeAvatar")
    Call<ResponseContainer<Success>> deletePhoto(@Field("access_token") String token);

    @Multipart
    @POST("wall.post")
    Call<ResponseContainer<ResponsePostPhoto>> postPicture(@Part("access_token") RequestBody action,
                                                           @Part("place_id") RequestBody placeId,
                                                           @Part("message") RequestBody message,
                                                           @Part("attachments") RequestBody attachments);


    @FormUrlEncoded
    @POST("wall.get")
    Call<ResponseContainer<ResponseWall>> getWallById(@Field("access_token") String token,
                                                      @Field("owner_id") String ownerId,
                                                      @Field("extended") String extended);
    @FormUrlEncoded
    @POST("wall.get")
    Call<ResponseContainer<ResponseWall>> getGroupWallById(@Field("access_token") String token,
                                                           @Field("place_id") String placeId,
                                                           @Field("filter") String filters,
                                                           @Field("extended") String extended,
                                                           @Field("fields") String fields,   //for users
                                                           @Field("offset") int offset,
                                                           @Field("count") int count);

    @FormUrlEncoded
    @POST("wall.get")
    Call<ResponseContainer<ResponseWall>> getWallExtended(@Field("access_token") String token,
                                                          @Field("offset") int offset,
                                                          @Field("count") int count,
                                                          @Field("extended") String extended);

    @FormUrlEncoded
    @POST("likes.add")
    Call<ResponseContainer<ResponseLike>> like(@Field("access_token") String token,
                                               @Field("type") String type,//post,photo
                                               @Field("item_id") String itemId,
                                               @Field("owner_id") String ownerId);

    @FormUrlEncoded
    @POST("likes.delete")
    Call<ResponseContainer<ResponseLike>> unlike(@Field("access_token") String token,
                                                 @Field("type") String type,
                                                 @Field("item_id") String itemId,
                                                 @Field("owner_id") String ownerId);

    @FormUrlEncoded
    @POST("events.get")
    Call<ResponseContainer<ResponseWall>> getEvents(@Field("access_token") String token,
                                                    @Field("group_id") String groupId,
                                                    @Field("extended") String type,
                                                    @Field("offset") int offset);


    @FormUrlEncoded
    @POST("events.getById")
    Call<ResponseContainer<ResponseEvents>> getEventsById(@Field("access_token") String token,
                                                          @Field("events") String events,
                                                          @Field("extended") String extended);


    @FormUrlEncoded
    @POST("events.getAll")
    Call<ResponseContainer<ResponseWall>> getAllEvents(@Field("access_token") String token,
                                                       @Field("city_id") int cityId,
                                                       @Field("q") String q,
                                                       @Field("extended") String type,
                                                       @Field("offset") int offset);

    @FormUrlEncoded
    @POST("events.getAll")
    Call<ResponseContainer<ResponseWall>> getAllEventsByUserId(@Field("access_token") String token,
                                                               @Field("city_id") int cityId,
                                                               @Field("q") String q,
                                                               @Field("extended") String type,
                                                               @Field("offset") int offset,
                                                               @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("events.addVisit")
    Call<ResponseContainer<ResponseVisit>> addVisit(@Field("access_token") String token,
                                                    @Field("event_id") String eventId,
                                                    @Field("owner_id") String ownerId);

    @FormUrlEncoded
    @POST("events.removeVisit")
    Call<ResponseContainer<ResponseVisit>> removeVisit(@Field("access_token") String token,
                                                       @Field("event_id") String eventId,
                                                       @Field("owner_id") String ownerId);

    @FormUrlEncoded
    @POST("events.getVisitors")
    Call<ResponseContainer<ResponseEventVisitors>> getVisitors(@Field("access_token") String token,
                                                               @Field("event_id") String eventId,
                                                               @Field("owner_id") String ownerId,
                                                               @Field("fields") String fields);

    @FormUrlEncoded
    @POST("actions.getAll")
    Call<ResponseContainer<ResponseWall>> getAllActions(@Field("access_token") String token,
                                                        @Field("city_id") int cityId,
                                                        @Field("q") String search,
                                                        @Field("extended") String type,
                                                        @Field("offset") int offset);

    @FormUrlEncoded
    @POST("services.getAll")
    Call<ResponseContainer<ResponseWall>> getAllServices(@Field("access_token") String token,
                                                         @Field("city_id") int cityId,
                                                         @Field("q") String search,
                                                         @Field("extended") String type,
                                                         @Field("offset") int offset);

    @FormUrlEncoded
    @POST("actions.getAll")
    Call<ResponseContainer<ResponseWall>> getAllActionsByGroupId(@Field("access_token") String token,
                                                                 @Field("extended") String type,
                                                                 @Field("offset") int offset,
                                                                 @Field("owner_id") String ownerId);

    @FormUrlEncoded
    @POST("services.getAll")
    Call<ResponseContainer<ResponseWall>> getAllServicesByGroupId(@Field("access_token") String token,
                                                                  @Field("extended") String type,
                                                                  @Field("offset") int offset,
                                                                  @Field("owner_id") String ownerId);
    @FormUrlEncoded
    @POST("actions.get")
    Call<ResponseContainer<ResponseEvents>> getActionById(@Field("access_token") String token,
                                                          @Field("actions") String events,
                                                          @Field("extended") String extended);

    @FormUrlEncoded
    @POST("services.get")
    Call<ResponseContainer<ResponseEvents>> getServiceById(@Field("access_token") String token,
                                                           @Field("services") String events,
                                                           @Field("extended") String extended);

    @FormUrlEncoded
    @POST("feed.get")
    Call<ResponseContainer<ResponseWall>> getFeed(@Field("access_token") String token,
                                                  @Field("city_id") int cityId,
                                                  @Field("q") String search,
                                                  @Field("extended") String type,
                                                  @Field("offset") int offset,
                                                  @Field("fields") String fields,
                                                  @Field("filter") String filter);


    @FormUrlEncoded
    @POST("wall.getComments")
    Call<ResponseContainer<ResponseComments>> getCommentsPost(@Field("access_token") String token,
                                                              @Field("sort") String sort,
                                                              @Field("post_id") String postId,
                                                              @Field("owner_id") String ownerId,
                                                              @Field("offset") int offset,
                                                              @Field("extended") String extended,
                                                              @Field("count") int count,
                                                              @Field("thread_items_count") int threadItemsCount,
                                                              @Field("fields") String fields);

    @FormUrlEncoded
    @POST("wall.getComments")
    Call<ResponseContainer<ResponseSubcomments>> getSubcommentsPost(@Field("access_token") String token,
                                                                    @Field("sort") String sort,
                                                                    @Field("post_id") String postId,
                                                                    @Field("owner_id") String ownerId,
                                                                    @Field("offset") int offset,
                                                                    @Field("extended") String extended,
                                                                    @Field("count") int count,
                                                                    @Field("thread_items_count") int threadItemsCount,
                                                                    @Field("comment_id") String commentId,
                                                                    @Field("fields") String fields);

    @FormUrlEncoded
    @POST("photos.getComments")
    Call<ResponseContainer<ResponseComments>> getCommentsPhoto(@Field("access_token") String token,
                                                               @Field("sort") String sort,
                                                               @Field("photo_id") String postId,
                                                               @Field("owner_id") String ownerId,
                                                               @Field("offset") int offset,
                                                               @Field("extended") String extended,
                                                               @Field("thread_items_count") int threadItemsCount,
                                                               @Field("count") int count,
                                                               @Field("fields") String fields);

    @FormUrlEncoded
    @POST("photos.getComments")
    Call<ResponseContainer<ResponseComments>> getSubcommentsPhoto(@Field("access_token") String token,
                                                                  @Field("sort") String sort,
                                                                  @Field("photo_id") String postId,
                                                                  @Field("owner_id") String ownerId,
                                                                  @Field("offset") int offset,
                                                                  @Field("extended") String extended,
                                                                  @Field("thread_items_count") int threadItemsCount,
                                                                  @Field("count") int count,
                                                                  @Field("comment_id") String commentId,
                                                                  @Field("fields") String fields);

    @FormUrlEncoded
    @POST("events.getComments")
    Call<ResponseContainer<ResponseComments>> getCommentsEvent(@Field("access_token") String token,
                                                               @Field("sort") String sort,
                                                               @Field("event_id") String eventId,
                                                               @Field("owner_id") String ownerId,
                                                               @Field("offset") int offset,
                                                               @Field("extended") String extended,
                                                               @Field("thread_items_count") int threadItemsCount,
                                                               @Field("count") int count,
                                                               @Field("fields") String fields);

    @FormUrlEncoded
    @POST("events.getComments")
    Call<ResponseContainer<ResponseComments>> getSubcommentsEvent(@Field("access_token") String token,
                                                                  @Field("sort") String sort,
                                                                  @Field("event_id") String eventId,
                                                                  @Field("owner_id") String ownerId,
                                                                  @Field("offset") int offset,
                                                                  @Field("extended") String extended,
                                                                  @Field("thread_items_count") int threadItemsCount,
                                                                  @Field("count") int count,
                                                                  @Field("comment_id") String commentId,
                                                                  @Field("fields") String fields);

    @FormUrlEncoded
    @POST("wall.createComment")
    Call<ResponseContainer<ResponseAddComment>> addPostComment(@Field("access_token") String token,
                                                               @Field("post_id") String postId,
                                                               @Field("owner_id") String ownerId,
                                                               @Field("text") String text);

    @FormUrlEncoded
    @POST("wall.createComment")
    Call<ResponseContainer<ResponseAddComment>> addReplyToPostComment(@Field("access_token") String token,
                                                                      @Field("post_id") String postId,
                                                                      @Field("owner_id") String ownerId,
                                                                      @Field("text") String text,
                                                                      @Field("reply_to_comment") String commentId);

    @FormUrlEncoded
    @POST("photos.createComment")
    Call<ResponseContainer<ResponseAddComment>> addPhotoComment(@Field("access_token") String token,
                                                                @Field("photo_id") String postId,
                                                                @Field("owner_id") String ownerId,
                                                                @Field("text") String text);

    @FormUrlEncoded
    @POST("photos.createComment")
    Call<ResponseContainer<ResponseAddComment>> addReplyToPhotoComment(@Field("access_token") String token,
                                                                       @Field("photo_id") String postId,
                                                                       @Field("owner_id") String ownerId,
                                                                       @Field("text") String text,
                                                                       @Field("reply_to_comment") String commentId);

    @FormUrlEncoded
    @POST("events.createComment")
    Call<ResponseContainer<ResponseAddComment>> addEventComment(@Field("access_token") String token,
                                                                @Field("event_id") String postId,
                                                                @Field("owner_id") String ownerId,
                                                                @Field("text") String text);
    @FormUrlEncoded
    @POST("events.createComment")
    Call<ResponseContainer<ResponseAddComment>> addReplyToEventComment(@Field("access_token") String token,
                                                                       @Field("event_id") String postId,
                                                                       @Field("owner_id") String ownerId,
                                                                       @Field("text") String text,
                                                                       @Field("reply_to_comment") String commentId);

    @FormUrlEncoded
    @POST("wall.deleteComment")
    Call<ResponseContainer<Success>> deleteComment(@Field("access_token") String token,
                                                   @Field("comment_id") String postId,
                                                   @Field("owner_id") String ownerId);

    @FormUrlEncoded
    @POST("photos.deleteComment")
    Call<ResponseContainer<Success>> deleteCommentPhoto(@Field("access_token") String token,
                                                        @Field("comment_id") String postId,
                                                        @Field("owner_id") String ownerId);

    @FormUrlEncoded
    @POST("messages.getSocketServer")
    Call<ResponseContainer<ResponseSocketServer>> getSocketServer(@Field("access_token") String token);

    @FormUrlEncoded
    @POST("groups.getAll")
    Call<ResponseContainer<ResponsePlaces>> getPlaces(@Field("access_token") String token,
                                                      @Field("offset") int offset,
                                                      @Field("city_id") int cityId,
                                                      @Field("category_id") int category,
                                                      @Field("order") String order,
                                                      @Field("q") String filter,
                                                      @Field("verified") int verified);
    @FormUrlEncoded
    @POST("groups.getAllForMap")
    Call<ResponseContainer<ResponsePlaces>> getPlacesForMap(@Field("access_token") String token,
                                                            @Field("city_id") int cityId,
                                                            @Field("offset") int offset,
                                                            @Field("period") int period);

    @FormUrlEncoded
    @POST("groups.get")
    Call<ResponseContainer<ResponsePlaces>> getPlacesByUserId(@Field("access_token") String token,
                                                              @Field("offset") int offset,
                                                              @Field("city_id") int cityId,
                                                              @Field("user_id") String user_id);


    @FormUrlEncoded
    @POST("groups.getbyId")
    Call<PlacesResponse> getPlaceByIds(@Field("access_token") String token,
                                       @Field("group_ids") String groupIds);

    @FormUrlEncoded
    @POST("groups.getMembers")
    Call<ResponseContainer<UsersContainer>> getPlaceSubscribers(@Field("access_token") String token,
                                                                @Field("offset") int offset,
                                                                @Field("group_id") String groupIds,
                                                                @Field("fields") String fields,
                                                                @Field("subscriptions_only") String subscriptions);

    @FormUrlEncoded
    @POST("groups.getMembersinplace")
    Call<ResponseContainer<UsersContainer>> getUsersInPlace(@Field("access_token") String token,
                                                            @Field("offset") int offset,
                                                            @Field("group_id") String groupIds,
                                                            @Field("fields") String fields,
                                                            @Field("subscriptions_only") String subscriptions);

    @FormUrlEncoded
    @POST("groups.join")
    Call<ResponseContainer<Success>> joinToGroup(@Field("access_token") String token,
                                                 @Field("group_id") String groupIds);

    @FormUrlEncoded
    @POST("groups.leave")
    Call<ResponseContainer<Success>>  leaveGroup(@Field("access_token") String token,
                                                 @Field("group_id") String groupIds);

    @FormUrlEncoded
    @POST("groups.getNearby")
    Call<ResponseContainer<ResponsePlaces>> getPlaceByCoordinates(@Field("access_token") String token,
                                                                  @Field("latitude") String latitude,
                                                                  @Field("longitude") String longitude,
                                                                  @Field("radius") int radius,
                                                                  @Field("verified") int verified);

    @FormUrlEncoded
    @POST("groups.getAllForMap")
    Call<ResponseContainer<ResponsePlaces>> getAllForMap(@Field("access_token") String token,
                                                                @Field("city_id") int cityId,
                                                                @Field("offset") int offset,
                                                                @Field("count") int count);

    @FormUrlEncoded
    @POST("groups.create")
    Call<ResponseContainer<SuccessCreatePlace>> createPlace(@Field("access_token") String token,
                                                            @Field("name") String name,
                                                            @Field("city_id") int cityId,
                                                            @Field("latitude") String latitude,
                                                            @Field("longitude") String longitude);

    @FormUrlEncoded
    @POST("account.setCoordinates")
    Call<ResponseContainer<UsersContainer>> setCoordinates(@Field("access_token") String token,
                                                           @Field("latitude") Double latitude,
                                                           @Field("longitude") Double longitude);

    @FormUrlEncoded
    @POST("database.getCategories")
    Call<PlaceCategoryResponce> getPlaceCategories(@Field("access_token") String token);

    @FormUrlEncoded
    @POST("notifications.get")
    Call<ResponseContainer<NotificationResponce>> getNotifications(@Field("access_token") String token,
                                                                   @Field("offset") int offset);

    @FormUrlEncoded
    @POST("groups.rate")
    Call<ResponseContainer<Success>> rate(@Field("access_token") String token,
                                          @Field("group_id") String groupIds,
                                          @Field("type") String service,
                                          @Field("rate") int rate);

    @FormUrlEncoded
    @POST("database.getCities")
    Call<ResponseContainer<ResponseCities>> getCitiesWithSearch(@Field("q") String search);



    @FormUrlEncoded
    @POST("account.registerDevice")
    Call<ResponseContainer<Success>> registerDevice(@Field("access_token") String token,
                                                    @Field("device_id") String deviceId,
                                                    @Field("token") String FCMToken);

    @FormUrlEncoded
    @POST("account.unregisterDevice")
    Call<ResponseContainer<Success>> unregisterDevice(@Field("access_token") String token,
                                                      @Field("device_id") String deviceId);

    @FormUrlEncoded
    @POST("account.getSettings")
    Call<ResponseContainer<SettingsResponse>> getSettings(@Field("access_token") String token);

    @FormUrlEncoded
    @POST("account.setSettings")
    Call<ResponseContainer<SettingsResponse>> sendSettings(@Field("access_token") String token,
                                                           @Field("new_message") int message,
                                                           @Field("like_post") int likePost,
                                                           @Field("like_comment") int likeComment,
                                                           @Field("follow") int follow,
                                                           @Field("comment_post") int commentPost,
                                                           @Field("place_event") int events,
                                                           @Field("place_action") int actions);

    @FormUrlEncoded
    @POST("account.saveProfileInfo")
    Call<ResponseContainer<Success>> saveProfileInfo(@Field("access_token") String token,
                                                     @Field("first_name") String firstName,
                                                     @Field("last_name") String lastName,
                                                     @Field("bdate") String birthday,
                                                     @Field("about") String about);


    @FormUrlEncoded
    @POST("wall.getById")
    Call<ResponseContainer<ResponseWall>> getPostById(@Field("access_token") String token,
                                                      @Field("posts") String postIds,
                                                      @Field("extended") String extended,
                                                      @Field("fields") String fields);

    @FormUrlEncoded
    @POST("claims.add")
    Call<ResponseContainer<Success>> claim(@Field("access_token") String token,
                                           @Field("object") String object,
                                           @Field("description") String description);


    @FormUrlEncoded
    @POST("account.ban")
    Call<ResponseContainer<Success>> ban(@Field("access_token") String token,
                                         @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("account.unban")
    Call<ResponseContainer<Success>> unban(@Field("access_token") String token,
                                           @Field("user_id") String userId);


    @FormUrlEncoded
    @POST("account.getBanned")
    Call<ResponseContainer<UsersContainer>> getBanned(@Field("access_token") String token,
                                                      @Field("user_id") String userId,
                                                      @Field("fields") String fields);

}
