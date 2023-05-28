package com.example.godribble.classes;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;
import java.util.Random;

public class SimpleLocation {

	public interface Listener {
		/* Called whenever the device's position changes so that you can call SimpleLocation#getPosition() */
		void onPositionChanged();
	}

	/** The internal name of the provider for the coarse location - approximate location*/
	private static final String PROVIDER_COARSE = LocationManager.NETWORK_PROVIDER;

	/** The internal name of the provider for the fine location - accurate location */
	private static final String PROVIDER_FINE = LocationManager.GPS_PROVIDER;

	/** The internal name of the provider for the fine location in passive mode */
	private static final String PROVIDER_FINE_PASSIVE = LocationManager.PASSIVE_PROVIDER;

	/** The default interval to receive new location updates after (in milliseconds) */
	private static final long INTERVAL_DEFAULT = 60 * 1000; // 1minutes. default -> 10 * 60 * 1000

	/** The PRNG that is used for location blurring */
	private static final Random mRandom = new Random();

	/** The last location that was internally cached when creating new instances in the same process */
	private static Location mCachedPosition;

	/** The LocationManager instance used to query the device location */
	private final LocationManager mLocationManager;
	/** Whether a fine location should be required or coarse location can be used */
	private final boolean mRequireFine;
	/** Whether passive mode shall be used or not */
	private final boolean mPassive;
	/** The internal after which new location updates are requested (in milliseconds) where longer intervals save battery */
	private final long mInterval;
	/** Whether to require a new location (`true`) or accept old (last known) locations as well (`false`) */
	private final boolean mRequireNewLocation;
	/** The LocationListener instance used internally to listen for location updates */
	private LocationListener mLocationListener;
	/** The current location with latitude, longitude, speed and altitude */
	private Location mPosition;
	private Listener mListener;

	public SimpleLocation(final Context context) {
		this(context, false);
	}

	public SimpleLocation(final Context context, final boolean requireFine) {
		this(context, requireFine, false);
	}

	public SimpleLocation(final Context context, final boolean requireFine, final boolean passive) {
		this(context, requireFine, passive, INTERVAL_DEFAULT);
	}

	public SimpleLocation(final Context context, final boolean requireFine, final boolean passive, final long interval) {
		this(context, requireFine, passive, interval, false);
	}

	public SimpleLocation(final Context context, final boolean requireFine, final boolean passive,
	                      final long interval, final boolean requireNewLocation) {
		mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		mRequireFine = requireFine;
		mPassive = passive;
		mInterval = interval;
		mRequireNewLocation = requireNewLocation;

		if (!mRequireNewLocation) {
			mPosition = getCachedPosition();
			if(mPosition!=null) {
				cachePosition();
			}
			else{
				beginUpdates();
			}
		}
	}

	public void setListener(final Listener listener) {
		mListener = listener;
	}

	private boolean hasLocationEnabled(final String providerName) {
		try {
			return mLocationManager.isProviderEnabled(providerName);
		}
		catch (Exception e) {
			return false;
		}
	}

	/** Starts updating the location and requesting new updates after the defined interval */
	@SuppressLint("MissingPermission")
	public void beginUpdates() {
		if (mLocationListener != null) {
			endUpdates();
		}

		if (!mRequireNewLocation) {
			mPosition = getCachedPosition();
		}

		mLocationListener = createLocationListener();
		mLocationManager.requestLocationUpdates(getProviderName(), mInterval, 0, mLocationListener);
	}

	/** Stops the location updates when they aren't needed anymore so that battery can be saved */
	@SuppressLint("MissingPermission")
	public void endUpdates() {
		if (mLocationListener != null) {
			mLocationManager.removeUpdates(mLocationListener);
			mLocationListener = null;
		}
	}

	public Point getPosition() {
		if (mPosition == null) {
			return null;
		}
		else {
			return new Point(mPosition.getLatitude(), mPosition.getLongitude());
		}
	}

	public double getLatitude() {
		if (mPosition == null) {
			return 0.0f;
		}
		else {
			return mPosition.getLatitude();
		}
	}

	public double getLongitude() {
		if (mPosition == null) {
			return 0.0f;
		}
		else {
			return mPosition.getLongitude();
		}
	}

	private LocationListener createLocationListener() {
		return new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				mPosition = location;
				cachePosition();
				if (mListener != null) {
					mListener.onPositionChanged();
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) { }

			@Override
			public void onProviderEnabled(String provider) { }

			@Override
			public void onProviderDisabled(String provider) { }

		};
	}

	private String getProviderName() {
		return getProviderName(mRequireFine);
	}

	private String getProviderName(final boolean requireFine) {
		// if fine location (GPS) is required
		if (requireFine) {
			// we just have to decide between active and passive mode
			if (mPassive) {
				return PROVIDER_FINE_PASSIVE;
			}
			else {
				return PROVIDER_FINE;
			}
		}
		// if both fine location (GPS) and coarse location (network) are acceptable
		else {
			// if we can use coarse location (network)
			if (hasLocationEnabled(PROVIDER_COARSE)) {
				// if we wanted passive mode
				if (mPassive) {
					// throw an exception because this is not possible
					throw new RuntimeException("There is no passive provider for the coarse location");
				}
				// if we wanted active mode
				else {
					// use coarse location (network)
					return PROVIDER_COARSE;
				}
			}
			// if coarse location (network) is not available
			else {
				// if we can use fine location (GPS)
				if (hasLocationEnabled(PROVIDER_FINE) || hasLocationEnabled(PROVIDER_FINE_PASSIVE)) {
					// we have to use fine location (GPS) because coarse location (network) was not available
					return getProviderName(true);
				}
				// no location is available so return the provider with the minimum permission level
				else {
					return PROVIDER_COARSE;
				}
			}
		}
	}

	@SuppressLint("MissingPermission")
	private Location getCachedPosition() {
		if (mCachedPosition != null) {
			return mCachedPosition;
		}
		else {
			try {
				//return mLocationManager.getLastKnownLocation(getProviderName());//it was used previously
				List<String> providers = mLocationManager.getProviders(true);
				Location bestLocation = null;
				for (String provider : providers) {
					Location l = mLocationManager.getLastKnownLocation(provider);
					if (l == null) {
						continue;
					}
					if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
						bestLocation = l;
					}
				}
				return bestLocation;
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	/** Caches the current position */
	private void cachePosition() {
		if (mPosition != null) {
			mCachedPosition = mPosition;
		}
	}

}