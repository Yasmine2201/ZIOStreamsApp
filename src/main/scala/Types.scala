import scala.Conversion

object Types {

  type Percentage = Float
  given Conversion[Float, Percentage] = Percentage(_)
  object Percentage {
    def apply(value: Float): Percentage = value
  }

  object CarbonIntensity {
    type gPerkWh = Float
    given Conversion[Float, gPerkWh] = gPerkWh(_)
    object gPerkWh {
      def apply(value: Float): gPerkWh = value
    }
  }

  object Power {
    type kW = Float
    type MW = Float
    type GW = Float

    object kW {
      def apply(value: Float): kW = value
      def toMW(value: kW): MW     = value / 1000
      def toGW(value: kW): GW     = value / 1000000
    }

    object MW {
      def apply(value: Float): MW = value
      def tokW(value: MW): kW     = value * 1000
      def toGW(value: MW): GW     = value / 1000
    }

    object GW {
      def apply(value: Float): GW = value
      def tokW(value: GW): kW     = value * 1000000
      def toMW(value: GW): MW     = value * 1000
    }

    given Conversion[Float, kW] = kW(_)
    given Conversion[Float, MW] = MW(_)
    given Conversion[Float, GW] = GW(_)

    implicit val kWOrdering: Ordering[kW] = (x: kW, y: kW) => x.toFloat compare y.toFloat
    implicit val MWOrdering: Ordering[MW] = (x: MW, y: MW) => x.toFloat compare y.toFloat
    implicit val GWOrdering: Ordering[GW] = (x: GW, y: GW) => x.toFloat compare y.toFloat
  }

  object Temperature {
    type Celsius = Float

    object Celsius {
      def apply(value: Float): Celsius = value
    }

    given Conversion[Float, Celsius]                = Celsius(_)
    implicit val CelsiusOrdering: Ordering[Celsius] = (x: Celsius, y: Celsius) => x.toFloat compare y.toFloat
  }
}
