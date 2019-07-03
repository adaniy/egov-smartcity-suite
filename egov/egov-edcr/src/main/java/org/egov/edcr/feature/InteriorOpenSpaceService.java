package org.egov.edcr.feature;

import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Room;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;

public class InteriorOpenSpaceService implements RuleService {
    private static Logger LOG = Logger.getLogger(InteriorOpenSpaceService.class);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        extractInteriorOpenSpace(pl, doc);
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        return null;
    }

    @Override
    public List<String> getParameters() {
        return null;
    }

    private void extractInteriorOpenSpace(PlanDetail pl, DXFDocument doc) {

        DXFLayer floorLayer = new DXFLayer();

        for (Block block : pl.getBlocks())
            for (Floor floor : block.getBuilding().getFloors()) {

                String floorName = DxfFileConstants.BLOCK_NAME_PREFIX + block.getNumber() + "_"
                        + DxfFileConstants.FLOOR_NAME_PREFIX
                        + floor.getNumber();
                floorLayer = doc.getDXFLayer(floorName);
                if (floorLayer != null) {
                    List dxfPolyLineEntities = floorLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
                    if (dxfPolyLineEntities != null)
                        for (Object dxfEntity : dxfPolyLineEntities) {
                            DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;
                            // floor.setPolyLine(dxflwPolyline); TODO: CHECK IS IT REQUIRED ?
                            if (dxflwPolyline.getColor() == DxfFileConstants.HABITABLE_ROOM_COLOR) {
                                Room habitable = new Room();
                                habitable.setPolyLine(dxflwPolyline);
                                floor.getHabitableRooms().add(habitable);
                            }
                            if (dxflwPolyline.getColor() == DxfFileConstants.FLOOR_EXTERIOR_WALL_COLOR) {
                                Measurement extWall = new Measurement();
                                extWall.setPolyLine(dxflwPolyline);
                                floor.setExterior(extWall);
                            }
                            if (dxflwPolyline.getColor() == DxfFileConstants.FLOOR_OPENSPACE_COLOR) {
                                Measurement openSpace = new Measurement();
                                openSpace.setPolyLine(dxflwPolyline);
                                floor.getOpenSpaces().add(openSpace);
                            }
                        }
                }

            }

    }
}
