package net.datatp.netty.rpc;

import org.junit.Test;

import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.google.protobuf.ByteString;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;

import net.datatp.netty.rpc.protocol.Request;
import net.datatp.netty.rpc.protocol.Response;
import net.datatp.netty.rpc.protocol.WirePayload;
import net.datatp.util.dataformat.DataSerializer;
import net.datatp.util.io.IOUtil;
import net.datatp.yara.MetricPrinter;
import net.datatp.yara.MetricRegistry;
import net.datatp.yara.Timer;

public class SerializationPerformanceTest {
  @Test
  public void testJacksonSerialization() throws Exception {
    DataSerializer serializer = new DataSerializer(new MappingJsonFactory(), new ProtobufModule()) ;
    WirePayload payload = createWirePayload() ;
    System.out.println(serializer.toString(payload));
  }
  
  @Test
  public void testPerformance() throws Exception {
    MetricRegistry mRegistry = new MetricRegistry() ;
    WirePayload payload = createWirePayload() ;
    System.out.println("ProtoBuf Size:   " + payload.toByteArray().length);
    System.out.println("Serializable Size: " + IOUtil.serialize(payload).length);
    for(int i = 0; i < 1000000; i++) {
      Timer.Context protoBufSerCtx = mRegistry.timer("protobuf", "serialize").time() ;
      byte[] protobufData = payload.toByteArray() ;
      protoBufSerCtx .close();
      
      Timer.Context protobufDeserCtx = mRegistry.timer("protobuf", "deserialize").time() ;
      WirePayload deserPayload = WirePayload.parseFrom(protobufData) ;
      protobufDeserCtx.close();
      
      Timer.Context javaSerCtx = mRegistry.timer("java", "serialize").time() ;
      byte[] javaData = IOUtil.serialize(payload) ;
      javaSerCtx .close();
      
      Timer.Context javaDeserCtx = mRegistry.timer("java", "deserialize").time() ;
      deserPayload = (WirePayload)IOUtil.deserialize(javaData) ;
      javaDeserCtx.close();
    }
    new MetricPrinter().print(mRegistry) ;
  }
  
  WirePayload createWirePayload() {
    WirePayload.Builder wBuilder = WirePayload.newBuilder() ;
    wBuilder.setCorrelationId(1l) ;
    wBuilder.setRequest(
        Request.newBuilder().
          setServiceId("AService").
          setMethodId("AMethod").
          setParams(ByteString.copyFrom("This is a test".getBytes())).build()
    ) ;

    wBuilder.setResponse(
      Response.newBuilder().
        setResult(ByteString.copyFrom("Response Data".getBytes())).
        build()
    ) ;
    return wBuilder.build() ;
  }
}