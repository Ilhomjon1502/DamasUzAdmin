package uz.ilhomjon.damasuzadmin

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import uz.ilhomjon.damasuzadmin.databinding.FragmentAddLiniyaMapsBinding
import uz.ilhomjon.damasuzadmin.models.Liniya

class AddLiniyaMapsFragment : Fragment() {

    private val TAG = "AddLiniyaMapsFragment"
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    var keyL = 0
    lateinit var liniya: Liniya

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        try {
            if (keyL==0)
            getDeviceLocation()
        }catch (e:Exception){
            Toast.makeText(context, "Avval GPS ni yoqing!!! \n${e.message}", Toast.LENGTH_SHORT).show()
        }

        if (keyL==1){
            liniya = arguments?.getSerializable("liniya") as Liniya
            binding.nameLiniya.text = liniya.name
            lineList.addAll(myLatLngToLatLng(liniya.locationListYoli!!))
            writePoliyline(lineList)

            mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        liniya.locationListYoli!![liniya.locationListYoli?.size!!/2].latitude!!,
                        liniya.locationListYoli!![liniya.locationListYoli?.size!!/2].longitude!!
                    ), 12.0f
                )
            )
        }

        mMap.setOnMapClickListener {
            lineList.add(it)
            writePoliyline(lineList)
        }

        binding.imageBack.setOnClickListener {
            if (lineList.isNotEmpty()) {
                lineList.removeLast()
                writePoliyline(lineList)
            }
        }

//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    lateinit var binding:FragmentAddLiniyaMapsBinding
    var lineList:ArrayList<LatLng> = ArrayList()
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var referenceLiniya: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddLiniyaMapsBinding.inflate(layoutInflater)

        keyL = arguments?.getInt("key", 0)!!

        firebaseDatabase = FirebaseDatabase.getInstance()
        referenceLiniya = firebaseDatabase.getReference("liniya")

        var name = ""
        if (keyL==0) {
            name = arguments?.getString("name")!!
            binding.nameLiniya.text = name
        }

        binding.imageSave.setOnClickListener {
            if (lineList.size > 5) {
                if (keyL==0) {
                    val key = referenceLiniya.push().key
                    val liniya = Liniya(key, name, latLngToMyLatLng(lineList))
                    referenceLiniya.child(key!!).setValue(liniya)
                    Toast.makeText(context, "$name saqlandi", Toast.LENGTH_SHORT).show()
                }else{
                    liniya.locationListYoli = latLngToMyLatLng(lineList)
                    referenceLiniya.child(liniya.id!!).setValue(liniya)
                    Toast.makeText(context, "${liniya.name} saqlandi", Toast.LENGTH_SHORT).show()
                }
                findNavController().popBackStack()
            } else {
                Toast.makeText(
                    context,
                    "Avval liniya yo'lini xaritaga chizing...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(binding.root.context)
    }


    private fun getDeviceLocation() {
        try {

            askPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION) {
                //all permissions already granted or just granted

                    val fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(binding.root.context)
                    val locationTask: Task<Location> = fusedLocationProviderClient.lastLocation
                    locationTask.addOnSuccessListener { it: Location ->
                        if (it != null) {
                            //We have a location
                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        it.latitude,
                                        it.longitude
                                    ), 15.0f
                                )
                            )
                        } else {
                            Log.d(
                                TAG,
                                "getLastLocation: location was null,,,,,,,,,,,,,,,,,,,..............."
                            )
                        }
                    }
                    locationTask.addOnFailureListener {
                        Log.d(TAG, "getLastLocation: ${it.message}")
                    }

            }.onDeclined { e ->
                if (e.hasDenied()) {

                    AlertDialog.Builder(context)
                        .setMessage("Please accept our permissions")
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


        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    var polyline1 : Polyline? = null
    fun writePoliyline(list:List<LatLng>){
        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.
        if (polyline1==null) {
            polyline1 = mMap.addPolyline(
                PolylineOptions().geodesic(true)
                    .clickable(true)
                    .addAll(list)
            )
        }else{
            polyline1?.remove()
            polyline1 = mMap.addPolyline(
                PolylineOptions().geodesic(true)
                    .clickable(true)
                    .addAll(list))
        }
    }
}