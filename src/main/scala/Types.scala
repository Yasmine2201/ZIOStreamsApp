import scala.Conversion

object Types {

  opaque type Percentage = Float
  given Conversion[Float, Percentage] = Percentage(_)
  object Percentage {
    def apply(value: Float): Percentage = value
  }

  object CarbonIntensity {
    opaque type gPerkWh = Float
    given Conversion[Float, gPerkWh] = gPerkWh(_)
    object gPerkWh {
      def apply(value: Float): gPerkWh = value
    }
  }

  object Power {
    opaque type kW = Float
    opaque type MW = Float
    opaque type GW = Float

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

  object Consumption {
    opaque type kWh = Float
    opaque type MWh = Float
    opaque type GWh = Float

    object kWh {
      def apply(value: Float): kWh = value
      def toMWh(value: kWh): MWh   = value / 1000
      def toGWh(value: kWh): GWh   = value / 1000000
    }

    object MWh {
      def apply(value: Float): MWh = value
      def tokWh(value: MWh): kWh   = value * 1000
      def toGWh(value: MWh): GWh   = value / 1000
    }

    object GWh {
      def apply(value: Float): GWh = value
      def tokWh(value: GWh): kWh   = value * 1000000
      def toMWh(value: GWh): MWh   = value * 1000
    }

    given Conversion[Float, kWh] = kWh(_)
    given Conversion[Float, MWh] = MWh(_)
    given Conversion[Float, GWh] = GWh(_)

    implicit val kWhOrdering: Ordering[kWh] = (x: kWh, y: kWh) => x.toFloat compare y.toFloat
    implicit val MWhOrdering: Ordering[MWh] = (x: MWh, y: MWh) => x.toFloat compare y.toFloat
    implicit val GWhOrdering: Ordering[GWh] = (x: GWh, y: GWh) => x.toFloat compare y.toFloat
  }

  object Temperature {
    opaque type Celsius = Float

    object Celsius {
      def apply(value: Float): Celsius = value
    }

    given Conversion[Float, Celsius]                = Celsius(_)
    implicit val CelsiusOrdering: Ordering[Celsius] = (x: Celsius, y: Celsius) => x.toFloat compare y.toFloat
  }
}
