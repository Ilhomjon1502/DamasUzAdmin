package uz.ilhomjon.damasuzadmin

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import uz.ilhomjon.damasuzadmin.databinding.FragmentMapsBinding
import uz.ilhomjon.damasuzadmin.models.Liniya
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.database.*
import uz.ilhomjon.damasuzadmin.databinding.ItemDialogMarkerBinding
import uz.ilhomjon.damasuzadmin.models.MyLatLng
import uz.ilhomjon.damasuzadmin.models.Shopir
import uz.ilhomjon.damasuzadmin.models.Yolovchi


class MapsFragment : Fragment() {

    private var mMap: GoogleMap? = null
    lateinit var geocoder: Geocoder
    private val TAG = "MapsActivity"
    val ACCES_FIND_LOCATION_CODE = 1000
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        writePolyline(myLatLngToLatLng(liniya.locationListYoli!!))

        binding.imgMapType.setOnClickListener {
            if (mMap?.mapType == GoogleMap.MAP_TYPE_HYBRID){
                mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            }else{
                mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
        }

        mMap?.setOnMarkerClickListener {
            if (markerListSh.values.contains(it)){
                var shp = Shopir()
                for (sh in shList) {
                    if (markerListSh[sh.id] == it){
                        shp = sh
                        break
                    }
                }

                val dialog = AlertDialog.Builder(context, R.style.NewDialog).create()
                val itemDialogMarkerBinding = ItemDialogMarkerBinding.inflate(layoutInflater)

                itemDialogMarkerBinding.tvName.text = shp.name
                itemDialogMarkerBinding.tvNumber.text = shp.phoneNumber
                itemDialogMarkerBinding.tvAvtoNumber.text = shp.avtoNumber
                itemDialogMarkerBinding.tvEmpty.text = "Odam soni: ${shp.boshJoy}"

                itemDialogMarkerBinding.tvNumber.setOnClickListener {
                    val posted_by = shp.phoneNumber
                    val uri = "tel:" + posted_by?.trim()
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(uri)
                    startActivity(intent)
                }

                dialog.setView(itemDialogMarkerBinding.root)
                dialog.show()
            }else if (markerListY.values.contains(it)){
                var yol = Yolovchi()
                for (sh in yList) {
                    if (markerListY[sh.id] == it){
                        yol = sh
                        break
                    }
                }

                val dialog = AlertDialog.Builder(context, R.style.NewDialog).create()
                val itemDialogMarkerBinding = ItemDialogMarkerBinding.inflate(layoutInflater)

                itemDialogMarkerBinding.tvName.text = yol.name
                itemDialogMarkerBinding.tvNumber.text = yol.number
                itemDialogMarkerBinding.tvAvtoNumber.visibility = View.GONE
                itemDialogMarkerBinding.tvEmpty.visibility = View.GONE

                itemDialogMarkerBinding.tvNumber.setOnClickListener {
                    val posted_by = yol.number
                    val uri = "tel:" + posted_by?.trim()
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(uri)
                    startActivity(intent)
                }

                dialog.setView(itemDialogMarkerBinding.root)
                dialog.show()
            }else{
                Toast.makeText(context, "Marker listda yo'q ya'ni bu siz", Toast.LENGTH_SHORT).show()
            }
            true
        }

        mMap?.setOnPolylineClickListener {
            Toast.makeText(context, "Bu chiziq ${liniya.name} liniya yo'li", Toast.LENGTH_LONG).show()
        }
    }

    var polyline1: Polyline? = null
    fun writePolyline(list: List<LatLng>) {
        if (polyline1 == null) {
            polyline1 = mMap?.addPolyline(
                PolylineOptions().geodesic(true)
                    .clickable(true)
                    .addAll(list)
            )
        } else {
            polyline1?.remove()
            polyline1 = mMap?.addPolyline(
                PolylineOptions().geodesic(true)
                    .clickable(true)
                    .addAll(list)
            )
        }
    }


    lateinit var liniya: Liniya
    lateinit var shopir: Shopir
    lateinit var binding:FragmentMapsBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var referenceShopir: DatabaseReference
    lateinit var referenceYolovchi: DatabaseReference

    lateinit var shList: ArrayList<Shopir>
    lateinit var yList: ArrayList<Yolovchi>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(layoutInflater)
        liniya = arguments?.getSerializable("keyLiniya") as Liniya
        shopir = arguments?.getSerializable("keyShopir") as Shopir

        firebaseDatabase = FirebaseDatabase.getInstance()
        referenceShopir = firebaseDatabase.getReference("shopir")
        referenceYolovchi = firebaseDatabase.getReference("yolovchi")

        geocoder = Geocoder(context)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create()
        locationRequest.setInterval(500)
        locationRequest.setFastestInterval(500)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        referenceShopir.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                shList = ArrayList()
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Shopir::class.java)
                    if (value != null) {
                        if (value.id != shopir.id && value.isOnline && value.liniyaId == liniya.id) {
                            shList.add(value)
                            addMarker(value)
                        }
                        if (!value.isOnline && markerListSh.keys.contains(value.id)){
                            markerListSh[value.id]?.remove()
                            markerListSh.remove(value.id)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Iltimos internetga qayta ulaning...", Toast.LENGTH_SHORT).show()
            }
        })

        referenceYolovchi.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                yList = ArrayList()
                val children = snapshot.children

                for (child in children) {
                    val value = child.getValue(Yolovchi::class.java)
                    if (value != null) {
                        if (value.location != null && value.liniyaId == liniya.id) {
                            yList.add(value)
                            addMarker(value)
                        }
                        if (value.location==null && markerListY.contains(value.id)){
                            markerListY[value.id]?.remove()
                            markerListY.remove(value.id)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        binding.imgSwipeUser.setOnClickListener {
            if (!userSwipe){
                binding.imgSwipeUser.setImageResource(R.drawable.ic_swipe_user_1)
                userSwipe = true
            }else{
                binding.imgSwipeUser.setImageResource(R.drawable.ic_swipe_user_0)
                userSwipe = false
            }
        }

        binding.btnPlus.setOnClickListener {
            shopir.boshJoy +=1
            binding.tvCount.text = shopir.boshJoy.toString()
        }
        binding.btnMinus.setOnClickListener {
            shopir.boshJoy -=1
            binding.tvCount.text = shopir.boshJoy.toString()
        }

        return binding.root
    }

    val markerListSh = HashMap<String, Marker>()

    fun addMarker(shopir: Shopir) {
        if (mMap != null) {
            if (markerListSh.keys.contains(shopir.id)){
                val marker = markerListSh[shopir.id]
                marker?.position = LatLng(shopir.location?.latitude!!, shopir.location?.longitude!!)
                marker?.title = "${shopir.name}\n${shopir.phoneNumber}\nodam soni: ${shopir.boshJoy}"
            }else {
                val markerOptions = mMap?.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                shopir.location?.latitude!!,
                                shopir.location?.longitude!!
                            )
                        )
                        .title("${shopir.name}\n${shopir.phoneNumber}\nodam soni: ${shopir.boshJoy}")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_damas))
                )
                markerListSh.put(shopir.id!!, markerOptions!!)
            }
        }
    }

    val markerListY = HashMap<String, Marker>()
    fun addMarker(yolovchi: Yolovchi) {
        if (mMap != null) {
            if (!markerListY.keys.contains(yolovchi.id)) {
                val markerOptions = mMap?.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                yolovchi.location?.latitude!!,
                                yolovchi.location?.longitude!!
                            )
                        )
                        .title("${yolovchi.name}\n${yolovchi.number}")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.yolovchi))
                )
                markerListY.put(yolovchi.id!!, markerOptions!!)
            }else{
                markerListY[yolovchi.id]?.position =  LatLng(
                    yolovchi.location?.latitude!!,
                    yolovchi.location?.longitude!!
                )
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            Log.d(TAG, "onLocationResult: ${p0.lastLocation}")
            if (mMap != null) {
                setUserLocationMarker(p0.lastLocation)
            }
        }
    }

    var userSwipe = false
    var userLocationMarker: Marker? = null
    var userLocationAcuracyCircle: Circle? = null
    fun setUserLocationMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (userLocationMarker == null) {
            // Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.located))
            //            markerOptions.rotation(location.bearing)
            markerOptions.anchor(0.5F, 0.5F)//mashinani o'rtaga qo'yish
            markerOptions.position(latLng)
            userLocationMarker = mMap?.addMarker(markerOptions)
        } else {
            // Use the previausly created marker
            userLocationMarker?.position = latLng
            //            userLocationMarker?.rotation = location.bearing // mashinani oldini harakat tomonga yo'naltirish
        }

        binding.tvCount.text = shopir.boshJoy.toString()
        val projection: Projection = mMap?.getProjection()!!
        val markerPosition: LatLng = latLng
        val markerPoint: Point = projection.toScreenLocation(markerPosition)
        val targetPoint = Point(markerPoint.x, markerPoint.y - view?.height!! / 3)
        val targetPosition = projection.fromScreenLocation(targetPoint)

        if (!userSwipe) {
            val currentPlace = CameraPosition.Builder()
                .target(targetPosition)
                .bearing(location.bearing).zoom(18f)
                .build()
//        mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace))

            mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 500, null)
        }

        val myLatLng = MyLatLng(latLng.latitude, latLng.longitude)
        shopir.location = myLatLng
        shopir.isOnline = true
        referenceShopir.child(shopir.id!!).setValue(shopir)

        if (userLocationAcuracyCircle == null) {
            val circleOptions = CircleOptions()
            circleOptions.center(latLng)
            circleOptions.strokeWidth(4F)
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
            circleOptions.fillColor(Color.argb(32, 255, 0, 0))
            circleOptions.radius(location.accuracy.toDouble())
            userLocationAcuracyCircle = mMap?.addCircle(circleOptions)
        } else {
            userLocationAcuracyCircle?.center = (latLng)
            userLocationAcuracyCircle?.radius = location.accuracy.toDouble()

        }
    }


    @SuppressLint("MissingPermission")
    fun startLocationUpdate() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()

        askPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) {
            //all permissions already granted or just granted
            startLocationUpdate()
        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(context)
                    .setMessage("Iltimos ushbu ruxsatlarni bering unda ilova sizni joylashuvingizni aniqlay olmaydi")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'

                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdate()
        shopir.location = null
        shopir.isOnline = false
        referenceShopir.child(shopir.id!!).setValue(shopir)
    }

    @SuppressLint("MissingPermission")
    fun enableUserLocation() {
        mMap?.isMyLocationEnabled = true
    }

    @SuppressLint("MissingPermission")
    fun zoomToUserLocation() {
        val locationTask = fusedLocationProviderClient.lastLocation
        locationTask.addOnSuccessListener {
            val latLng = LatLng(it.latitude, it.longitude)
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0F))
            //            mMap.addMarker(MarkerOptions().position(latLng))
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCES_FIND_LOCATION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
                zoomToUserLocation()
            } else {
                //We can show a dialog that permission is not granted....
            }
        }
    }
}