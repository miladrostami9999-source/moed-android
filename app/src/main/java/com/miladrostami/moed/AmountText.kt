package com.miladrostami.moed

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*

@Composable fun AmountText(value:Long,modifier:Modifier=Modifier,fontSize:TextUnit=18.sp,color:Color=Color.Unspecified,fontWeight:FontWeight=FontWeight.Bold){
 Text(groupedAmount(value),modifier,fontSize=fontSize,color=color,fontWeight=fontWeight,maxLines=1,overflow=TextOverflow.Ellipsis,
  style=TextStyle(textDirection=TextDirection.Ltr,fontFamily=FontFamily.SansSerif,fontFeatureSettings="tnum",textAlign=if(LocalLayoutDirection.current==LayoutDirection.Rtl)TextAlign.End else TextAlign.Start))
}
