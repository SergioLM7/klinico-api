package com.sergio.klinico.application.services;

import com.sergio.klinico.domain.exceptions.BusinessException;
import com.sergio.klinico.domain.models.Admission;
import com.sergio.klinico.domain.models.Episode;
import com.sergio.klinico.domain.models.PaginatedResult;
import com.sergio.klinico.domain.repositories.AdmissionRepository;
import com.sergio.klinico.domain.repositories.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación que gestiona los episodios clínicos de la ronda médica ({@link Episode}).
 *
 * <p>Un episodio representa una nota de evolución vinculada a un ingreso activo,
 * registrada por un médico durante la ronda hospitalaria. Contiene el progreso clínico,
 * diagnóstico y scores de valoración (Braden, CAM, CHADS2).</p>
 *
 * <p>Reglas de negocio:</p>
 * <ul>
 *   <li>Solo se puede crear un episodio si el ingreso asociado existe.</li>
 *   <li>Solo el médico que creó el episodio puede modificarlo
 *       (validado mediante {@code Episode#validateUpdate}).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EpisodeService {

    private final AdmissionRepository admissionRepository;
    private final EpisodeRepository episodeRepository;

    /**
     * Crea un nuevo episodio clínico asociado al ingreso indicado.
     *
     * <p>Verifica que el ingreso exista antes de guardar el episodio y actualiza
     * la relación bidireccional en el objeto de dominio.</p>
     *
     * @param admissionId UUID del ingreso al que pertenece el episodio
     * @param newEpisode  datos del episodio a crear
     * @return episodio persistido con los datos generados (ID, timestamps, etc.)
     * @throws BusinessException si el ingreso con el {@code admissionId} indicado no existe
     */
    @Transactional
    public Episode create(UUID admissionId, Episode newEpisode) {
        Admission admission = admissionRepository.findById(admissionId);

        if (admission == null) {
            log.error("La admisión {}, que está intentando modificar, no existe", admissionId);
            throw new BusinessException("La admisión solicitada no está en BD");
        }

        admission.addEpisode(newEpisode);

        return episodeRepository.save(newEpisode);
    }

    /**
     * Devuelve los episodios de un ingreso de forma paginada, ordenados por fecha descendente.
     *
     * @param admissionId UUID del ingreso
     * @param page        número de página (0-indexed)
     * @return resultado paginado con 5 episodios por página
     */
    public PaginatedResult<Episode> getEpisodesByAdmission(UUID admissionId, int page) {
        return episodeRepository.findAllByAdmission(admissionId, page, 5);
    }

    /**
     * Devuelve los episodios de un ingreso registrados en una fecha concreta.
     *
     * @param admissionId UUID del ingreso
     * @param episodeDate fecha de creación del episodio
     * @return lista de episodios registrados en esa fecha (puede estar vacía)
     */
    public List<Episode> getEpisodeByEpisodeDate(UUID admissionId, LocalDate episodeDate) {
        return episodeRepository.findByEpisodeDate(admissionId, episodeDate);
    }

    /**
     * Actualiza los datos clínicos de un episodio existente.
     *
     * <p>Valida que el médico que intenta actualizar sea el mismo que lo creó, mediante la
     * lógica de dominio {@code Episode#validateUpdate(UUID)}. Si la validación falla
     * se lanza una {@link BusinessException}.</p>
     *
     * @param updatedData            objeto con los nuevos datos clínicos del episodio
     * @param episodeId              UUID del episodio a actualizar
     * @param doctorIdAttemptingUpdate UUID del médico que solicita la modificación
     * @return episodio actualizado
     * @throws BusinessException si el episodio no existe o el médico no es su autor
     */
    @Transactional
    public Episode update(Episode updatedData, UUID episodeId, UUID doctorIdAttemptingUpdate) {

        Episode currentEpisode = episodeRepository.findById(episodeId);

        if(currentEpisode == null) {
            log.error("El episodio {}, que está intentando actualizar, no existe", episodeId);
            throw new BusinessException("El episodio que se intenta actualizar no existe en BD");
        }

        currentEpisode.validateUpdate(doctorIdAttemptingUpdate);

        currentEpisode.updateClinicalData(updatedData);

        return episodeRepository.save(currentEpisode);
    }
}
